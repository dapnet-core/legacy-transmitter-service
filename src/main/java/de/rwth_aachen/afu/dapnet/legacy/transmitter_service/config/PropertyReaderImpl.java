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

import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * This class provides simplified methods to access properties.
 * 
 * @author Philipp Thiel
 */
final class PropertyReaderImpl implements PropertyReader {

	private final Properties properties;

	/**
	 * Creates a new instance of the adapter using the given {@link Properties}
	 * object.
	 * 
	 * @param properties Properties to use.
	 */
	public PropertyReaderImpl(Properties properties) {
		this.properties = Objects.requireNonNull(properties, "properties");
	}

	@Override
	public Optional<Boolean> getBoolean(String key) {
		String value = properties.getProperty(key);
		if (value != null && !value.isEmpty()) {
			return Optional.of(Boolean.parseBoolean(value));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public Optional<Double> getDouble(String key) {
		String value = properties.getProperty(key);
		if (value != null && !value.isEmpty()) {
			return Optional.of(Double.parseDouble(value));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public Optional<Integer> getInteger(String key) {
		String value = properties.getProperty(key);
		if (value != null && !value.isEmpty()) {
			return Optional.of(Integer.parseInt(value));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> getString(String key) {
		String value = properties.getProperty(key);
		if (value != null && !value.isEmpty()) {
			return Optional.of(value);
		} else {
			return Optional.empty();
		}
	}

}
