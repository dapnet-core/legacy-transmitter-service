/*
 * DAPNET Legacy Transmitter Service
 * Copyright (C) 2021 Philipp Thiel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.rwth_aachen.afu.dapnet.legacy.transmitter_service.backend;

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

	/**
	 * Checks if the request completed successfully.
	 * 
	 * @return True on success, false if an error is set
	 */
	public boolean isOk() {
		return error == null;
	}

}
