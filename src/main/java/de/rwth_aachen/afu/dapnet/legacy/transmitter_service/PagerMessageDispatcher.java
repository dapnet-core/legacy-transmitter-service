package de.rwth_aachen.afu.dapnet.legacy.transmitter_service;

import org.dapnet.core.transmission.messages.PagerMessage;

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
