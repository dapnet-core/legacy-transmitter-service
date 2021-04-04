package de.rwth_aachen.afu.dapnet.legacy.transmitter_service.backend;

import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.transmission.PagerMessage;

/**
 * Message dispatcher interface.
 * 
 * @author Philipp Thiel
 */
@FunctionalInterface
public interface PagerMessageDispatcher {

	/**
	 * Dispatches a pager message.
	 * 
	 * @param message         Message to dispatch
	 * @param transmitterName Name of the destination transmitter
	 */
	void dispatchMessage(PagerMessage message, String transmitterName);

}
