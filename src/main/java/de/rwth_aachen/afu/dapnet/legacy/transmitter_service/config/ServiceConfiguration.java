package de.rwth_aachen.afu.dapnet.legacy.transmitter_service.config;

public class ServiceConfiguration extends Configuration {

	private String amqpHost;
	private String amqpUser;
	private String amqpPassword;
	private String amqpExchangeName;

	private String bootstrapUrl;
	private String heartbeatUrl;

	private int serverPort = 43434;
	private int numSyncLoops = 5;
	private int sendSpeed = 1; // 0: 512, 1: 1200, 2:2400

	@Override
	public void loadConfiguration(PropertyReader reader) {
		amqpHost = reader.getString("amqp.host").orElseThrow();
		amqpUser = reader.getString("amqp.user").orElseThrow();
		amqpPassword = reader.getString("amqp.password").orElseThrow();
		amqpExchangeName = reader.getString("amqp.exchange").orElseThrow();

		bootstrapUrl = reader.getString("services.bootstrap").orElseThrow();
		heartbeatUrl = reader.getString("services.heartbeat").orElseThrow();

		serverPort = reader.getInteger("serverPort").orElse(43434);
		numSyncLoops = reader.getInteger("numberOfSyncLoops").orElse(5);
		sendSpeed = reader.getInteger("sendSpeed").orElse(1);
	}

	public String getAmqpHost() {
		return amqpHost;
	}

	public String getAmqpUser() {
		return amqpUser;
	}

	public String getAmqpPassword() {
		return amqpPassword;
	}

	public String getAmqpExchangeName() {
		return amqpExchangeName;
	}

	public String getBootstrapUrl() {
		return bootstrapUrl;
	}

	public String getHeartbeatUrl() {
		return heartbeatUrl;
	}

	public int getNumSyncLoops() {
		return numSyncLoops;
	}

	public int getServerPort() {
		return serverPort;
	}

	public int getNumberOfSyncLoops() {
		return numSyncLoops;
	}

	public int getSendSpeed() {
		return sendSpeed;
	}

}
