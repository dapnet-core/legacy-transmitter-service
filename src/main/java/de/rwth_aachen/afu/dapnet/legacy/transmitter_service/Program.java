package de.rwth_aachen.afu.dapnet.legacy.transmitter_service;

import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.transmission.TransmitterManager;
import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.transmission.TransmitterServer;

public class Program {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final String PROGRAM_VERSION;
	private static volatile Program program;
	private volatile TransmitterManager transmitterManager;
	private volatile TransmitterServer transmitterServer;

	static {
		String ver = Program.class.getPackage().getImplementationVersion();
		if (ver != null) {
			PROGRAM_VERSION = ver;
		} else {
			PROGRAM_VERSION = "UNKNOWN";
		}
	}

	private void start() {
		try {
			LOGGER.info("Starting DAPNET Legacy Transmitter Service {} ...", PROGRAM_VERSION);

			LOGGER.info("Starting transmitter manager");
			transmitterManager = new TransmitterManager();

			LOGGER.info("Starting transmitter server");
			transmitterServer = new TransmitterServer(transmitterManager);
			transmitterServer.start();

			LOGGER.info("Startup complete");

		} catch (CoreStartupException e) {
			LOGGER.fatal("Failed to start Legacy Transmitter Service: {}", e.getMessage());
			System.exit(1);
		} catch (Exception e) {
			LOGGER.fatal("Failed to start Legacy Transmitter Service.", e);
			System.exit(1);
		}
	}

	private void stop() {
		LOGGER.info("Stopping legacy-transmitter-service ...");

		if (transmitterServer != null) {
			transmitterServer.stop();
		}

		LOGGER.info("legacy-transmitter-service stopped");
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
					if (program != null) {
						program.stop();
					}
				} catch (Exception ex) {
					LOGGER.fatal("Exception while stopping backwardcompatibilityservice.", ex);
				}

				// Shutdown log4j
				LogManager.shutdown();
			}
		});

		program = new Program();
		program.start();

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
		if (program != null) {
			program.stop();
		}
	}

}
