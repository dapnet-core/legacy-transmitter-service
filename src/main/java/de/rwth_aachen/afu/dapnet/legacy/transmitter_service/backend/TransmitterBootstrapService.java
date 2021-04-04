package de.rwth_aachen.afu.dapnet.legacy.transmitter_service.backend;

/**
 * Transmitter bootstrap service interface.
 * 
 * @author Philipp Thiel
 */
@FunctionalInterface
public interface TransmitterBootstrapService {

	/**
	 * Posts a transmitter bootstrap request.
	 * 
	 * @param callSign Transmitter call sign
	 * @param authKey  Transmitter auth key
	 * @param type     Transmitter type name
	 * @param version  Transmitter version
	 * @return Service Bootstrap result (timeslots) or status error in status code
	 */
	ServiceResult<String> postBootstrapRequest(String callSign, String authKey, String type, String version);

}
