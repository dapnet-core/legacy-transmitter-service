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

package org.dapnet.backwardcompatibilityservice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.backwardcompatibilityservice.transmission.RabbitMQManager;
import org.dapnet.backwardcompatibilityservice.transmission.TransmissionManager;
import org.dapnet.backwardcompatibilityservice.transmission.TransmitterServer;

import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackwardCompatibilityService {

	private static final Logger logger = LogManager.getLogger();
	private static final String CORE_VERSION;
	private static final String API_VERSION;
	private static volatile BackwardCompatibilityService backwardcompatibilityservice;
	private volatile TransmissionManager transmissionManager;
	private volatile TransmitterServer transmitterServer;
	private volatile RabbitMQManager rabbitmqmanager;

	static {
		String ver = BackwardCompatibilityService.class.getPackage().getImplementationVersion();
		if (ver != null) {
			CORE_VERSION = ver;
		} else {
			CORE_VERSION = "UNKNOWN";
		}

		// Extract API version from Core version
		// Use getSpecificationVersion instead?
		Pattern versionPattern = Pattern.compile("(\\d+\\.\\d+\\.\\d+)\\p{Graph}*");
		Matcher m = versionPattern.matcher(CORE_VERSION);
		if (m.matches()) {
			API_VERSION = m.group(1);
		} else {
			API_VERSION = CORE_VERSION;
		}
	}

	private void start(boolean enforceStartup) {
		try {
			logger.info("Starting backward-compatibility-service Version {} ...", CORE_VERSION);

			logger.info("Starting TransmissionManager");
			transmissionManager = new TransmissionManager();

			logger.info("Starting Transmitter Server");
			transmitterServer = new TransmitterServer(transmissionManager.getTransmitterManager());
			transmitterServer.start();

            logger.info("Starting RabbitQM Manager");
            rabbitmqmanager = new RabbitMQManager("dapnet.calls");
            rabbitmqmanager.addRabbitMQQueue("db0wa");
			rabbitmqmanager.addRabbitMQQueue("db0kwe");

			logger.info("backward-compatibility-service started");

		} catch (CoreStartupException e) {
			logger.fatal("Failed to start backward-compatibility-service: {}", e.getMessage());
			System.exit(1);
		} catch (Exception e) {
			logger.fatal("Failed to start backward-compatibility-service.", e);
			System.exit(1);
		}
	}

	private void stop() {
		logger.info("Stopping backward-compatibility-service ...");

		if (transmitterServer != null) {
			transmitterServer.stop();
		}

		logger.info("backward-compatibility-service stopped");
	}

	public static void main(String[] args) throws Exception {
		// Disable IPv6 for Java VM, creates sometimes LogMessages
		System.setProperty("java.net.preferIPv4Stack", "true");

		// Jersey and Hibernate do not support log4j2, so setting additionally
		// Java Logger to warn level
		setJavaLogLevelToWarn();

		// Set language to English
		Locale.setDefault(Locale.ENGLISH);

		// Register shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread("ShutdownHook") {
			@Override
			public void run() {
				try {
					if (backwardcompatibilityservice != null) {
						backwardcompatibilityservice.stop();
					}
				} catch (Exception ex) {
					logger.fatal("Exception while stopping backwardcompatibilityservice.", ex);
				}

				// Shutdown log4j
				LogManager.shutdown();
			}
		});

		// Check args
		boolean enforceStartup = false;
		for (String arg : args) {
			if (arg.equals("--enforce-startup")) {
				enforceStartup = true;
			}
		}

		backwardcompatibilityservice = new BackwardCompatibilityService();
		backwardcompatibilityservice.start(enforceStartup);

	}

	private static void setJavaLogLevelToWarn() {
		java.util.logging.Logger topLogger = java.util.logging.Logger.getLogger("");
		// Handler for console (reuse it if it already exists)
		Handler consoleHandler = null;
		// see if there is already a console handler
		for (Handler handler : topLogger.getHandlers()) {
			if (handler instanceof ConsoleHandler) {
				consoleHandler = handler;
				break;
			}
		}
		if (consoleHandler == null) {
			// no console handler found, create a new one
			consoleHandler = new ConsoleHandler();
			topLogger.addHandler(consoleHandler);
		}
		// set the console handler to fine:
		consoleHandler.setLevel(Level.WARNING);
	}

	public static void shutdown() {
		if (backwardcompatibilityservice != null) {
			backwardcompatibilityservice.stop();
		}
	}

	public static String getCoreVersion() {
		return CORE_VERSION;
	}

	public static String getApiVersion() {
		return API_VERSION;
	}

}
