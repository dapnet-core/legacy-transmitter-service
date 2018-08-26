/*
 * DAPNET CORE PROJECT
 * Copyright (C) 2016
 *
 * Daniel Sialkowski
 *
 * daniel.sialkowski@rwth-aachen.de
 *
 * Institute of High Frequency Technology
 * RWTH AACHEN UNIVERSITY
 * Melatener Str. 25
 * 52074 Aachen
 */

package org.dapnet.backwardcompatibilityservice.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.backwardcompatibilityservice.Settings;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class State implements Serializable {

	private static final long serialVersionUID = 7604901183837032119L;
	private static final Logger logger = LogManager.getLogger();
	private static final Gson gson;

	static {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
//		builder.registerTypeAdapterFactory(new GsonTypeAdapterFactory());
		gson = builder.create();
	}

	@NotNull(message = "nicht vorhanden")
	@Valid
	private ConcurrentMap<String, CallSign> callSigns = new ConcurrentHashMap<>();


	@NotNull(message = "nicht vorhanden")
	@Valid
	private ConcurrentMap<String, User> users = new ConcurrentHashMap<>();

	@NotNull(message = "nicht vorhanden")
	@Valid
	private Collection<Call> calls;

	@NotNull(message = "nicht vorhanden")
	@Valid
	private ConcurrentMap<String, Transmitter> transmitters = new ConcurrentHashMap<>();


	public State() {
		calls = Collections.synchronizedList(new ArrayList<>());

		setModelReferences();
	}

	public void setModelReferences() {
		// Setting reference to state in model for allow returning of reference
		// instead of strings
		Call.setState(this);
		CallSign.setState(this);
		Transmitter.setState(this);
	}

	public static State readFromFile() throws Exception {
		try (InputStreamReader reader = new InputStreamReader(
				new FileInputStream(Settings.getModelSettings().getStateFile()), "UTF-8")) {
			return gson.fromJson(reader, State.class);
		}
	}

	public void writeToFile() {
		File file = new File(Settings.getModelSettings().getStateFile());
		try {
			if (file.getParentFile() != null) {
				file.getParentFile().mkdirs();
			}

			try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
				writer.write(gson.toJson(this));
				writer.flush();
			}

			logger.info("Successfully wrote state to file");
		} catch (Exception e) {
			logger.fatal("Failed to write state file: ", e);
		}
	}

	public Collection<Call> getCalls() {
		return calls;
	}

	public ConcurrentMap<String, CallSign> getCallSigns() {
		return callSigns;
	}


	public ConcurrentMap<String, User> getUsers() {
		return users;
	}

	public ConcurrentMap<String, Transmitter> getTransmitters() {
		return transmitters;
	}
}
