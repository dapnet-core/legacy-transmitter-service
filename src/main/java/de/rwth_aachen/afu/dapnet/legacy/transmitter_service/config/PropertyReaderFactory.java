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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This class provides factory methods for property readers.
 * 
 * @author Philipp Thiel
 */
public final class PropertyReaderFactory {

	private PropertyReaderFactory() {
	}

	/**
	 * Creates a new property reader from a properties file.
	 * 
	 * @param configFile Configuration file to load
	 * @throws IOException if the configuration file could not be loaded.
	 */
	public static PropertyReader fromFile(String configFile) throws IOException {
		Properties props = new Properties();
		try (FileInputStream in = new FileInputStream(configFile)) {
			props.load(in);
		}

		return new PropertyReaderImpl(props);
	}

}
