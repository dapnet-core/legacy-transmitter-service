package de.rwth_aachen.afu.dapnet.legacy.transmitter_service.amqp;

/**
 * Interface for managing transmitter message queues.
 * 
 * @author Philipp Thiel
 */
public interface TransmitterMessageQueueManager {

	/**
	 * Binds the message queue for the given transmitter.
	 * 
	 * @param transmitterName Transmitter name
	 * @return True if the binding was successful.
	 */
	boolean bindTransmitterQueue(String transmitterName);

	/**
	 * Cancels a binding for a transmitter message queue.
	 * 
	 * @param transmitterName Transmitter name
	 * @return True if the binding has been canceled.
	 */
	boolean cancelTransmitterQueue(String transmitterName);

}
