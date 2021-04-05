package de.rwth_aachen.afu.dapnet.legacy.transmitter_service;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.backend.RabbitMQManager;
import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.backend.TransmitterServices;
import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.config.PropertyReader;
import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.config.PropertyReaderFactory;
import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.config.ServiceConfiguration;
import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.transmission.TransmitterManager;
import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.transmission.TransmitterServer;

/**
 * This class contains the application entry point of the DAPNET Legacy
 * Transmitter Service.
 * 
 * @author Philipp Thiel
 */
public class Program {

	private static final Logger LOGGER = LogManager.getLogger();
	private volatile ThreadedPagerMessageDispatcher messageDispatcher;
	private volatile RabbitMQManager mqManager;
	private volatile TransmitterManager transmitterManager;
	private volatile TransmitterServer transmitterServer;

	private static ServiceConfiguration loadConfigurationFile() {
		final String configFile = System.getProperty("dapnet.config_file", "legacy-service.properties");

		LOGGER.info("Using configuration file '{}'", configFile);

		try {
			PropertyReader reader = PropertyReaderFactory.fromFile(configFile);

			ServiceConfiguration config = new ServiceConfiguration();
			config.loadConfiguration(reader);

			return config;
		} catch (IOException ex) {
			throw new CoreStartupException("Failed to load configuration file: " + configFile, ex);
		}
	}

	private void start() {
		try {
			final String version = Program.class.getPackage().getImplementationVersion();
			LOGGER.info("Starting DAPNET Legacy Transmitter Service {} ...", version);

			final ServiceConfiguration config = loadConfigurationFile();
			final TransmitterServices transmitterServices = new TransmitterServices(config);

			LOGGER.info("Starting message queue manager");
			messageDispatcher = new ThreadedPagerMessageDispatcher(2);
			mqManager = new RabbitMQManager(config, messageDispatcher);

			LOGGER.info("Starting transmitter manager");
			transmitterManager = new TransmitterManager(config, transmitterServices, mqManager);
			messageDispatcher.setConsumer(transmitterManager::sendMessage);

			LOGGER.info("Starting transmitter server");
			transmitterServer = new TransmitterServer(transmitterManager);
			transmitterServer.start();

			LOGGER.info("Startup completed");
		} catch (CoreStartupException e) {
			LOGGER.fatal("Failed to start Legacy Transmitter Service: {}", e.getMessage());
			System.exit(1);
		} catch (Exception e) {
			LOGGER.fatal("Failed to start Legacy Transmitter Service.", e);
			System.exit(1);
		}
	}

	private void stop() {
		LOGGER.info("Stopping the service ...");

		// Order is important here
		// First, shutdown the RabbitMQ manager, so no new messages are forwarded to the
		// dispatcher
		if (mqManager != null) {
			mqManager.shutdown();
		}

		// Next the message dispatcher must be stopped. This will wait until all worker
		// threads have finished.
		if (messageDispatcher != null) {
			messageDispatcher.shutdown();
		}

		// Finally, stop the transmitter service. This will disconnect all transmitter
		// clients
		if (transmitterServer != null) {
			transmitterServer.shutdown();
		}

		LOGGER.info("The service has been stopped.");
	}

	public static void main(String[] args) throws Exception {
		// Disable IPv6 for Java VM, creates sometimes LogMessages
		System.setProperty("java.net.preferIPv4Stack", "true");

		// Jersey and Hibernate do not support log4j2, so setting additionally
		// Java Logger to warn level
		setJavaLogLevelToWarn();

		// Set language to English
		Locale.setDefault(Locale.ENGLISH);

		Program program = new Program();

		// Register shutdown hook
		Runnable shutdownTask = () -> {
			try {
				program.stop();
			} catch (Exception ex) {
				LOGGER.fatal("Exception while stopping backwardcompatibilityservice.", ex);
			}

			// Shutdown log4j
			LogManager.shutdown();
		};

		Runtime.getRuntime().addShutdownHook(new Thread(shutdownTask, "ShutdownHook"));

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

}
