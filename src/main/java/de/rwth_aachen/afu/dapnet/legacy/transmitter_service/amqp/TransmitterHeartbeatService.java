package de.rwth_aachen.afu.dapnet.legacy.transmitter_service.amqp;

/**
 * Interface for transmitter heartbeat services.
 * 
 * @author Philipp Thiel
 */
@FunctionalInterface
public interface TransmitterHeartbeatService {

	/**
	 * Posts the heartbeat for a transmitter.
	 * 
	 * @param callSign  Transmitter call sign.
	 * @param authKey   Transmitter authentication key
	 * @param ntpSynced NTP synced yes/no
	 * @return True if the heartbeat post request was successful
	 */
	boolean postHeartbeat(String callSign, String authKey, boolean ntpSynced);

}
