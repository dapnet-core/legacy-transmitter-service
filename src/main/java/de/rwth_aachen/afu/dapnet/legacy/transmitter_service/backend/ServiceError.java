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
