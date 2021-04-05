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

package de.rwth_aachen.afu.dapnet.legacy.transmitter_service.config;

import java.util.Optional;

/**
 * This interface provides methods to read properties.
 * 
 * @author Philipp Thiel
 */
public interface PropertyReader {

	/**
	 * Gets a boolean from the configuration.
	 * 
	 * @param key Key to look for.
	 * @return Configuration value or empty optional if not found.
	 */
	Optional<Boolean> getBoolean(String key);

	/**
	 * Gets a double from the configuration.
	 * 
	 * @param key Key to look for.
	 * @return Configuration value or empty optional if not found.
	 */
	Optional<Double> getDouble(String key);

	/**
	 * Gets an integer from the configuration.
	 * 
	 * @param key Key to look for.
	 * @return Configuration value or empty optional if not found.
	 */
	Optional<Integer> getInteger(String key);

	/**
	 * Gets a string from the configuration.
	 * 
	 * @param key Key to look for.
	 * @return Configuration value or empty optional if not found.
	 */
	Optional<String> getString(String key);

}
