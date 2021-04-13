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

package de.rwth_aachen.afu.dapnet.legacy.transmitter_service.config;

/**
 * Service configuration parameters.
 * 
 * @author Philipp Thiel
 */
public class ServiceConfiguration extends Configuration {

	private String amqpHost;
	private String amqpUser;
	private String amqpPassword;
	private String amqpExchangeName;

	private String bootstrapUrl;
	private String heartbeatUrl;

	private int serverPort = 43434;
	private int numSyncLoops = 5;

	@Override
	public void loadConfiguration(PropertyReader reader) {
		// RabbitMQ settings
		amqpHost = reader.getString("amqp.host").orElseThrow();
		amqpUser = reader.getString("amqp.user").orElseThrow();
		amqpPassword = reader.getString("amqp.password").orElseThrow();
		amqpExchangeName = reader.getString("amqp.exchange").orElse("dapnet.local_calls");

		// Service URLs
		bootstrapUrl = reader.getString("services.bootstrap").orElseThrow();
		heartbeatUrl = reader.getString("services.heartbeat").orElseThrow();

		// Transmitter server settings
		serverPort = reader.getInteger("serverPort").orElse(43434);
		numSyncLoops = reader.getInteger("numberOfSyncLoops").orElse(5);
	}

	/**
	 * Gets the host name for the RabbitMQ server.
	 * 
	 * @return Host name
	 */
	public String getAmqpHost() {
		return amqpHost;
	}

	/**
	 * Gets the user name for the RabbitMQ server.
	 * 
	 * @return User name
	 */
	public String getAmqpUser() {
		return amqpUser;
	}

	/**
	 * Gets the password for the RabbitMQ server.
	 * 
	 * @return Password
	 */
	public String getAmqpPassword() {
		return amqpPassword;
	}

	/**
	 * Gets the exchange name for the RabbitMQ server.
	 * 
	 * @return Exchange name
	 */
	public String getAmqpExchangeName() {
		return amqpExchangeName;
	}

	/**
	 * Gets the transmitter bootstrap service URL
	 * 
	 * @return Transmitter bootstrap service URL
	 */
	public String getBootstrapUrl() {
		return bootstrapUrl;
	}

	/**
	 * Gets the transmitter heartbeat service URL.
	 * 
	 * @return Transmitter heartbeat service URL
	 */
	public String getHeartbeatUrl() {
		return heartbeatUrl;
	}

	/**
	 * Gets the port the transmitter server should listen on.
	 * 
	 * @return Transmitter server port
	 */
	public int getServerPort() {
		return serverPort;
	}

	/**
	 * Gets the number of sync loops for the transmitter handshake.
	 * 
	 * @return Number of sync loops to perform
	 */
	public int getNumberOfSyncLoops() {
		return numSyncLoops;
	}

}
