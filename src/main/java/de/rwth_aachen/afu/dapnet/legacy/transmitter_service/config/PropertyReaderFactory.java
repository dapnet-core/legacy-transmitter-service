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
