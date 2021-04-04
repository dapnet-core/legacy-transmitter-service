package de.rwth_aachen.afu.dapnet.legacy.transmitter_service.backend;

/**
 * Service error response consisting of a status code and an optional error
 * message.
 * 
 * @author Philipp Thiel
 */
public final class ServiceError {

	private final int statusCode;
	private final String errorMessage;

	/**
	 * Creates a new service error object.
	 * 
	 * @param statusCode   Status code
	 * @param errorMessage Error message
	 */
	public ServiceError(int statusCode, String errorMessage) {
		this.statusCode = statusCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Gets the status code.
	 * 
	 * @return Status code
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * Gets the error message.
	 * 
	 * @return Error message
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

}
