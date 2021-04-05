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

package de.rwth_aachen.afu.dapnet.legacy.transmitter_service.transmission;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.backend.TransmitterBootstrapService;
import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.backend.TransmitterHeartbeatService;
import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.backend.TransmitterMessageQueueManager;
import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.backend.TransmitterServices;
import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.config.ServiceConfiguration;
import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.transmission.Transmitter.Status;

/**
 * This class manages connected transmitters.
 * 
 * @author Philipp Thiel
 */
public class TransmitterManager {
	private static final Logger logger = LogManager.getLogger();
	private final ConcurrentMap<String, TransmitterClient> connectedClients = new ConcurrentHashMap<>();
	private final ServiceConfiguration configuration;
	private final TransmitterServices transmitterServices;
	private final TransmitterMessageQueueManager mqManager;

	/**
	 * Constructs a new transmitter manager instance.
	 * 
	 * @param configManager       Configuration manager to use
	 * @param transmitterServices Transmitter services to use
	 * @throws NullPointerException if transmitterServices is {@code null}
	 */
	public TransmitterManager(ServiceConfiguration configuration, TransmitterServices transmitterServices,
			TransmitterMessageQueueManager mqManager) {
		this.configuration = Objects.requireNonNull(configuration, "Transmitter configuration must not be null.");
		this.transmitterServices = Objects.requireNonNull(transmitterServices,
				"Transmitter services must not be null.");
		this.mqManager = Objects.requireNonNull(mqManager, "Message queue manager must not be null.");
	}

	/**
	 * Gets the transmitter configuration.
	 * 
	 * @return Transmitter configuration
	 */
	public ServiceConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * Gets the transmitter heartbeat service.
	 * 
	 * @return Transmitter heartbeat service
	 */
	public TransmitterHeartbeatService getHearbeatService() {
		return transmitterServices;
	}

	/**
	 * Gets the transmitter bootstrap service.
	 * 
	 * @return Transmitter bootstrap service
	 */
	public TransmitterBootstrapService getBootstrapService() {
		return transmitterServices;
	}

	/**
	 * Gets an unmodifyable collection of the currently connected clients.
	 * 
	 * @return Collection of transmitter clients
	 */
	public Collection<TransmitterClient> getConnectedClients() {
		return Collections.unmodifiableCollection(connectedClients.values());
	}

	/**
	 * Sends a message to all connected transmitters.
	 * 
	 * @param message Message to send.
	 */
	public void sendMessage(PagerMessage message) {
		connectedClients.values().forEach(c -> c.sendMessage(message));
	}

	/**
	 * Sends messages to all connected transmitters.
	 * 
	 * @param messages Messages to send
	 */
	public void sendMessages(Collection<PagerMessage> messages) {
		connectedClients.values().forEach(c -> c.sendMessages(messages));
	}

	/**
	 * Sends a message to a specific connected transmitter.
	 * 
	 * @param message         Message to send.
	 * @param transmitterName Transmitter name.
	 */
	public void sendMessage(PagerMessage message, String transmitterName) {
		TransmitterClient cl = connectedClients.get(NamedObject.normalize(transmitterName));
		if (cl != null) {
			cl.sendMessage(message);
		}
	}

	/**
	 * Sends multiple messages to a specific connected transmitter.
	 * 
	 * @param messages        Messages to send.
	 * @param transmitterName Transmitter name.
	 */
	public void sendMessages(Collection<PagerMessage> messages, String transmitterName) {
		TransmitterClient cl = connectedClients.get(NamedObject.normalize(transmitterName));
		if (cl != null) {
			cl.sendMessages(messages);
		}
	}

	/**
	 * Callback to handle connect events.
	 * 
	 * @param client Transmitter client to add.
	 */
	public void onConnect(TransmitterClient client) {
		Transmitter t = client.getTransmitter();
		if (t == null) {
			logger.warn("Client has no associated transmitter object.");
			client.close().syncUninterruptibly();
			return;
		}

		t.setStatus(Status.ONLINE);

		Instant lastConnected = Instant.now();
		t.setLastConnected(lastConnected);
		t.setConnectedSince(lastConnected);

		final String transmitterName = t.getNormalizedName();
		connectedClients.put(transmitterName, client);

		logger.debug("Binding queue for transmitter '{}'", transmitterName);

		try {
			if (!mqManager.bindTransmitterQueue(transmitterName)) {
				logger.error("Failed to bind to queue for transmitter '{}'", transmitterName);
			}
		} catch (Exception ex) {
			logger.error("Failed to bind queue for transmitter.", ex);
		}
	}

	/**
	 * Callback that handles disconnect events.
	 * 
	 * @param client Transmitter to remove.
	 */
	public void onDisconnect(TransmitterClient client) {
		final Transmitter t = client.getTransmitter();
		if (t == null) {
			return;
		}

		if (t.getStatus() != Status.ERROR) {
			t.setStatus(Status.OFFLINE);
		}

		t.setConnectedSince(null);

		final String transmitterName = t.getNormalizedName();
		connectedClients.remove(transmitterName);

		logger.debug("Canceling queue for transmitter '{}'", transmitterName);

		try {
			if (!mqManager.cancelTransmitterQueue(transmitterName)) {
				logger.error("Failed to cancel queue for transmitter '{}'", transmitterName);
			}
		} catch (Exception ex) {
			logger.error("Failed to cancel queue for transmitter.", ex);
		}
	}

	/**
	 * Disconnects from all connected transmitters.
	 */
	public void disconnectFromAll() {
		connectedClients.values().forEach(cl -> cl.close().syncUninterruptibly());
	}

	/**
	 * Disconnects from the given transmitter.
	 * 
	 * @param t Transmitter to disconnect from.
	 */
	public void disconnectFrom(Transmitter t) {
		TransmitterClient cl = connectedClients.remove(t.getNormalizedName());
		if (cl != null) {
			cl.close().syncUninterruptibly();
		}
	}

}
