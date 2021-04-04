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
