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
