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
