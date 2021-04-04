package de.rwth_aachen.afu.dapnet.legacy.transmitter_service.transmission;

import java.util.Locale;

/**
 * Interface for objects with a (unique) name.
 * 
 * @author Philipp Thiel
 */
public interface NamedObject {
	/**
	 * Gets the name of the object.
	 * 
	 * @return Name or {@code null} if not set
	 */
	String getName();

	/**
	 * Gets the normalized name of the object. This applies
	 * {@link #normalize(String)} to {@link #getName()}.
	 * 
	 * @return Normalized name or {@code null} if {@code getName() == null}
	 */
	default String getNormalizedName() {
		String name = getName();
		if (name != null) {
			name = normalize(name);
		}

		return name;
	}

	/**
	 * Normalizes a string by converting to lower case.
	 * 
	 * @param text Text
	 * @return Normalized string or {@code null} if {@code text == null}
	 */
	static String normalize(String text) {
		if (text != null) {
			text = text.toLowerCase(Locale.ROOT);
		}

		return text;
	}
}
