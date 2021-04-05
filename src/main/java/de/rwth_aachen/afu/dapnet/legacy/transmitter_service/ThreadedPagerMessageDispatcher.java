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

/**
 * A pager message dispatcher that uses an executor service to dispatch incoming
 * messages to a consumer.
 * 
 * @author Philipp Thiel
 */
final class ThreadedPagerMessageDispatcher implements PagerMessageDispatcher {

	private static final Logger LOGGER = LogManager.getLogger();
	private final ExecutorService executor;
	private volatile BiConsumer<PagerMessage, String> consumer = null;

	/**
	 * Constructs a new message dispatcher instance.
	 * 
	 * @param numThreads Number of threads for the executor service
	 */
	public ThreadedPagerMessageDispatcher(int numThreads) {
		executor = Executors.newFixedThreadPool(numThreads);
	}

	/**
	 * Stops the message dispatcher. This will wait until all pending tasks have
	 * been finished.
	 */
	public void shutdown() {
		LOGGER.info("Shutting down executor service.");
		executor.shutdown();

		// Wait until work has finished
		try {
			executor.awaitTermination(30, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			LOGGER.error("Waiting for thread has been interrupted.");
		}
	}

	/**
	 * Sets the pager message consumer. The incoming pager messages will be
	 * dispatched to this consumer by the worker threads.
	 * 
	 * @param consumer Pager message consumer
	 */
	public void setConsumer(BiConsumer<PagerMessage, String> consumer) {
		this.consumer = consumer;
	}

	@Override
	public void dispatchMessage(PagerMessage message, String transmitterName) {
		if (message == null || transmitterName == null) {
			LOGGER.warn("Cannot dispatch pager message, message or transmitter name is null.");
			return;
		}

		try {
			executor.execute(new DispatchMessageTask(message, transmitterName));
		} catch (RejectedExecutionException ex) {
			// Usually occurs if shutdown() has been called
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
