package de.rwth_aachen.afu.dapnet.legacy.transmitter_service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.backend.PagerMessageDispatcher;
import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.transmission.PagerMessage;

class ThreadedPagerMessageDispatcher implements PagerMessageDispatcher {

	private static final Logger LOGGER = LogManager.getLogger();
	private final ExecutorService executor = Executors.newFixedThreadPool(2);
	private volatile BiConsumer<PagerMessage, String> consumer = null;

	public void shutdown() {
		LOGGER.info("Shutting down executor service.");
		executor.shutdown();

		// Wait until work has finished
		try {
			executor.awaitTermination(30, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			LOGGER.error("Thread has been interrupted.");
		}
	}

	public void setConsumer(BiConsumer<PagerMessage, String> consumer) {
		this.consumer = consumer;
	}

	@Override
	public void dispatchMessage(PagerMessage message, String transmitterName) {
		if (message == null || transmitterName == null) {
			LOGGER.warn("Invalid message received, message or transmitter name is null.");
			return;
		}

		Runnable task = new DispatchMessageTask(message, transmitterName);
		try {
			executor.execute(task);
		} catch (RejectedExecutionException ex) {
			// Usually if shutdown has been called
			LOGGER.warn("Could not send message to transmitter '{}', task was rejected by executor.", transmitterName);
		}
	}

	private class DispatchMessageTask implements Runnable {

		private final PagerMessage message;
		private final String transmitterName;

		public DispatchMessageTask(PagerMessage message, String transmitterName) {
			this.message = message;
			this.transmitterName = transmitterName;
		}

		@Override
		public void run() {
			final BiConsumer<PagerMessage, String> theConsumer = consumer;
			if (theConsumer != null) {
				try {
					theConsumer.accept(message, transmitterName);
				} catch (Exception ex) {
					LOGGER.error("Exception in pager message consumer.", ex);
				}
			} else {
				LOGGER.warn("Could not dispatch pager message, consumer is null.");
			}
		}

	}

}
