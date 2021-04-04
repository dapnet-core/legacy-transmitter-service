package de.rwth_aachen.afu.dapnet.legacy.transmitter_service.amqp;

/**
 * Wrapper for a service result. The result consists of an error object or a
 * payload object.
 * 
 * @author Philipp Thiel
 *
 * @param <T> Payload object type
 */
public class ServiceResult<T> {

	private final ServiceError error;
	private final T value;

	/**
	 * Constructs a new service with an error result.
	 * 
	 * @param statusCode   Status code
	 * @param errorMessage Error message
	 */
	public ServiceResult(int statusCode, String errorMessage) {
		error = new ServiceError(statusCode, errorMessage);
		value = null;
	}

	/**
	 * Constructs a new service result with a value result.
	 * 
	 * @param value Payload object
	 */
	public ServiceResult(T value) {
		error = null;
		this.value = value;
	}

	/**
	 * Gets the service error.
	 * 
	 * @return Service error
	 */
	public ServiceError getError() {
		return error;
	}

	/**
	 * Gets the payload object.
	 * 
	 * @return Payload object
	 */
	public T getValue() {
		return value;
	}

	/**
	 * Checks if an error is present.
	 * 
	 * @return True if an error is set, false otherwise.
	 */
	public boolean hasError() {
		return error != null;
	}

}
