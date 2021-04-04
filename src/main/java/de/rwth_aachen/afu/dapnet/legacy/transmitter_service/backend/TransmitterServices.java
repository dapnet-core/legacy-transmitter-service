package de.rwth_aachen.afu.dapnet.legacy.transmitter_service.backend;

import java.io.StringReader;
import java.util.Objects;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientResponse;

import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.config.ServiceConfiguration;

public class TransmitterServices implements TransmitterHeartbeatService, TransmitterBootstrapService {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final int MAX_TIMESLOTS = 16;
	private final ServiceConfiguration configuration;

	public TransmitterServices(ServiceConfiguration configuration) {
		this.configuration = Objects.requireNonNull(configuration, "Service configuration must not be null.");
	}

	@Override
	public boolean postHeartbeat(String callSign, String authKey, boolean ntpSynced) {
		Client postClient = ClientBuilder.newClient();
		WebTarget webResource = postClient.target(configuration.getHeartbeatUrl());
		JsonObjectBuilder postRequest = Json.createObjectBuilder();
		postRequest.add("callsign", callSign);
		postRequest.add("auth_key", authKey);
		postRequest.add("ntp_synced", ntpSynced);

		JsonObject postJson = postRequest.build();

		String postJsonString = postJson.toString();
		// System.out.println(postJsonString);

		ClientResponse response = webResource.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(postJsonString, MediaType.APPLICATION_JSON), ClientResponse.class);

		if (response.getStatus() == 200) {
			return true;
		} else {
			LOGGER.error("Heartbeat service returned error status code {} for transmitter '{}'", response.getStatus(),
					callSign);

			return false;
		}
	}

	@Override
	public ServiceResult<String> postBootstrapRequest(String callSign, String authKey, String type, String version) {
		Client postClient = ClientBuilder.newClient();
		WebTarget webResource = postClient.target(configuration.getBootstrapUrl());

		JsonObjectBuilder postRequest = Json.createObjectBuilder();
		postRequest.add("callsign", callSign);
		postRequest.add("auth_key", authKey);

		JsonObjectBuilder softwareInfo = Json.createObjectBuilder();
		softwareInfo.add("name", type);
		softwareInfo.add("version", version);

		postRequest.add("software", softwareInfo);
		JsonObject postJson = postRequest.build();

		String postJsonString = postJson.toString();
		// System.out.println(POSTRequestString);

		ClientResponse response = webResource.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(postJsonString, MediaType.APPLICATION_JSON), ClientResponse.class);

		String responseJson = response.readEntity(String.class);

		// System.out.println(response.toString());

		JsonObject responseJsonObject = null;
		try (JsonReader jsonReader = Json.createReader(new StringReader(responseJson))) {
			responseJsonObject = jsonReader.readObject();
		}

		switch (response.getStatus()) {
		case 200:
		case 201:
			// Created
			String timeslots = getTimeslotsFromJson(responseJsonObject.getJsonArray("timeslots"));
			return new ServiceResult<>(timeslots);
		default:
			JsonString errorMessage = responseJsonObject.getJsonString("error");
			return new ServiceResult<>(response.getStatus(), errorMessage != null ? errorMessage.getString() : null);
		}
	}

	private String getTimeslotsFromJson(JsonArray timeslotsArray) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < timeslotsArray.size() && i < MAX_TIMESLOTS; ++i) {
			if (timeslotsArray.getBoolean(i)) {
				sb.append(Integer.toHexString(i).toUpperCase());
			}
		}

		return sb.toString();
	}

}
