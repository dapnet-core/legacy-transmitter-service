/*
 * DAPNET Legacy Transmitter Service
 * Copyright (C) 2021 Philipp Thiel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.rwth_aachen.afu.dapnet.legacy.transmitter_service.backend;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeoutException;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.config.ServiceConfiguration;
import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.transmission.PagerMessage;
import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.transmission.PagerMessage.ContentType;
import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.transmission.PagerMessage.Priority;
import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.transmission.PagerMessage.SubAddress;

/**
 * Transmitter message queue manager implementation.
 * 
 * @author Philipp Thiel
 *
 */
public class RabbitMQManager implements TransmitterMessageQueueManager {

	private static final Logger LOGGER = LogManager.getLogger();
	private final ConcurrentMap<String, String> activeQueues = new ConcurrentHashMap<>();
	private final String exchange;
	private final PagerMessageDispatcher messageDispatcher;
	private final Connection connection;
	private final Channel channel;

	/**
	 * Constructs a new object instance.
	 * 
	 * @param settings          Connection settings for RabbitMQ
	 * @param messageDispatcher Message dispatcher instance
	 * @throws NullPointerException if settings or the message dispatcher is
	 *                              {@code null}
	 * @throws IOException          May be caused by connecting to the message queue
	 *                              host
	 * @throws TimeoutException     May be caused by connecting to the message queue
	 *                              host
	 */
	public RabbitMQManager(ServiceConfiguration configuration, PagerMessageDispatcher messageDispatcher)
			throws IOException, TimeoutException {
		if (configuration == null) {
			throw new NullPointerException("Service configuration must not be null.");
		}

		this.exchange = Objects.requireNonNull(configuration.getAmqpExchangeName(), "Exchange name must not be null.");
		this.messageDispatcher = Objects.requireNonNull(messageDispatcher, "Message dispatcher must not be null.");

		// Create the connection
		final ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(configuration.getAmqpHost());
		factory.setUsername(configuration.getAmqpUser());
		factory.setPassword(configuration.getAmqpPassword());

		connection = factory.newConnection();
		channel = connection.createChannel();

		// Declare the exchange (must exist)
		AMQP.Exchange.DeclareOk declareResult = channel.exchangeDeclarePassive(exchange);
		LOGGER.debug("Exchange declare result: {}", declareResult);

		// connect("dapnetdc2.db0sda.ampr.org", "node-db0sda-dc2", "73mxX4JLttzmVZ2");
	}

	public void shutdown() {
		cancelActiveQueues();

		try {
			channel.close();
		} catch (IOException | TimeoutException ex) {
			LOGGER.error("Failed to close the channel.", ex);
		}

		try {
			connection.close();
		} catch (IOException ex) {
			LOGGER.error("Failed to close the connection.", ex);
		}
	}

	@Override
	public boolean bindTransmitterQueue(String transmitterName) {
		if (activeQueues.containsKey(transmitterName)) {
			LOGGER.warn("Transmitter '{}' already registered.", transmitterName);
			return false;
		}

		// Declare the queue
		AMQP.Queue.DeclareOk declareResult;
		try {
			final Map<String, Object> args = new TreeMap<>();
			args.put("x-expires", 1800000); // TODO Configurable?

			declareResult = channel.queueDeclare(transmitterName, false, false, false, args);
			LOGGER.debug("Queue declare result: {}", declareResult);
		} catch (IOException ex) {
			LOGGER.error("Failed to declare queue.", ex);
			return false;
		}

		// Bind the queue
		try {
			AMQP.Queue.BindOk bindResult = channel.queueBind(declareResult.getQueue(), exchange, transmitterName);
			LOGGER.debug("Bind result: {}", bindResult);
		} catch (IOException ex) {
			LOGGER.error("Failed to bind queue.", ex);
			return false;
		}

		// Register the new queue as active
		activeQueues.put(transmitterName, declareResult.getQueue());

		// Start the queue
		try {
			final String tag = channel.basicConsume(declareResult.getQueue(), new CallMessageConsumer(channel));
			LOGGER.debug("New consumer tag: {}", tag);
		} catch (IOException ex) {
			LOGGER.error("Failed to start consume.", ex);
			return false;
		}

		return true;
	}

	@Override
	public boolean cancelTransmitterQueue(String transmitterName) {
		final String queueName = activeQueues.remove(transmitterName);
		if (queueName == null) {
			LOGGER.warn("Transmitter '{}' is not registered.");
			return false;
		}

		try {
			channel.basicCancel(queueName);
			// TODO Delete queue?
			return true;
		} catch (IOException ex) {
			LOGGER.error("Failed to cancel transmitter queue.", ex);
			return false;
		}
	}

	private void cancelActiveQueues() {
		// TODO Implementation
	}

	/**
	 * Call message consumer implementation.
	 * 
	 * @author Philipp Thiel
	 */
	private class CallMessageConsumer extends DefaultConsumer {

		public CallMessageConsumer(Channel channel) {
			super(channel);
		}

		@Override
		public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
				throws IOException {
			// Extract the JSON message
			JsonObject msgObj = null;
			try (final InputStream instr = new ByteArrayInputStream(body);
					final JsonReader reader = Json.createReader(instr)) {
				msgObj = reader.readObject();
			}

			// Check the protocol
			if (!msgObj.containsKey("protocol") || !"pocsag".equals(msgObj.getString("protocol"))) {
				LOGGER.error("Not a pocsag protocol message.");
				return;
			}

			// Create the pager message
			PagerMessage message = createPagerMessage(msgObj.getJsonObject("message"));
			if (message == null) {
				// Error logging is performed in method
				return;
			}

			// Dispatch the message
			messageDispatcher.dispatchMessage(message, envelope.getRoutingKey());
		}

		/**
		 * Creates a pager message from the JSON message object. Errors will be logged.
		 * 
		 * @param messageObject JSON message object.
		 * @return Pager message or {@code null} on errors.
		 */
		private PagerMessage createPagerMessage(final JsonObject messageObject) {
			if (messageObject == null) {
				LOGGER.error("Message object not found in JSON.");
				return null;
			}

			final JsonString data = messageObject.getJsonString("data");
			if (data == null) {
				LOGGER.error("String 'data' not found in JSON message.");
				return null;
			}

			final JsonNumber ric = messageObject.getJsonNumber("ric");
			if (ric == null) {
				LOGGER.error("Number 'ric' not found in JSON message.");
				return null;
			}

			final JsonNumber function = messageObject.getJsonNumber("function");
			if (function == null) {
				LOGGER.error("Number 'function' not found in JSON message.");
				return null;
			}

			SubAddress subAddr;
			try {
				subAddr = SubAddress.fromValue(function.intValue());
			} catch (IllegalArgumentException ex) {
				LOGGER.error("Unsupported function/sub-address: {}", function.intValue());
				return null;
			}

			return new PagerMessage(Priority.CALL, ric.intValue(), subAddr, ContentType.ALPHANUMERIC, data.getString());
		}

	}

}
