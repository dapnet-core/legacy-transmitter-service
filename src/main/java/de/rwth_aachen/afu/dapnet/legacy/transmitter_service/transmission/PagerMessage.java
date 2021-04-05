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

import java.time.Instant;

/**
 * This class represents a pager message.
 * 
 * @author Philipp Thiel
 */
public class PagerMessage implements Comparable<PagerMessage> {
	/**
	 * Enumeration of message priorities. This can be used to prioritize messages in
	 * waiting queues. It does not affect the content of the transmitted radio
	 * message.
	 * 
	 * @author Philipp Thiel
	 */
	public enum Priority {
		EMERGENCY, TIME, CALL, NEWS, ACTIVATION, RUBRIC
	}

	/**
	 * The message content type.
	 * 
	 * @author Philipp Thiel
	 */
	public enum ContentType {
		NUMERIC, ALPHANUMERIC
	}

	/**
	 * Enumeration of the POCSAG sub-address.
	 * 
	 * @author Philipp Thiel
	 *
	 */
	public enum SubAddress {
		ADDR_A(0), ADDR_B(1), ADDR_C(2), ADDR_D(3);

		private int value;

		private SubAddress(int value) {
			this.value = value;
		}

		/**
		 * Gets the integer value of the sub-address.
		 * 
		 * @return Integer value
		 */
		public int getValue() {
			return value;
		}

		/**
		 * Gets a sub-address from an integer value.
		 * 
		 * @param value Integer value (0-3)
		 * @return Sub-address
		 * @throws IllegalArgumentException if {@code value} is outside the allowed
		 *                                  range.
		 */
		public static SubAddress fromValue(int value) {
			switch (value) {
			case 0:
				return ADDR_A;
			case 1:
				return ADDR_B;
			case 2:
				return ADDR_C;
			case 3:
				return ADDR_D;
			default:
				throw new IllegalArgumentException("Not a valid sub-address.");
			}
		}
	}

	private final Instant timestamp;
	private final Priority priority;
	private final int address;
	private final SubAddress subAddress;
	private final ContentType type;
	private final String content;

	/**
	 * Constructs a new pager message.
	 * 
	 * @param timestamp  Timestamp (used for message ordering)
	 * @param priority   Priority (used for message ordering)
	 * @param address    Destination address
	 * @param subAddress Sub-address
	 * @param type       Content type
	 * @param content    Message content
	 */
	public PagerMessage(Instant timestamp, Priority priority, int address, SubAddress subAddress, ContentType type,
			String content) {
		this.timestamp = timestamp;
		this.priority = priority;
		this.address = address;
		this.subAddress = subAddress;
		this.type = type;
		this.content = content;
	}

	/**
	 * Constructs a new pager message.
	 * 
	 * @param priority   Priority (used for message ordering)
	 * @param address    Destination address
	 * @param subAddress Sub-address
	 * @param type       Content type
	 * @param content    Message content
	 */
	public PagerMessage(Priority priority, int address, SubAddress subAddress, ContentType type, String content) {
		this(Instant.now(), priority, address, subAddress, type, content);
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public ContentType getContentType() {
		return type;
	}

	public Priority getPriority() {
		return priority;
	}

	public int getAddress() {
		return address;
	}

	public SubAddress getSubAddress() {
		return subAddress;
	}

	public String getContent() {
		return content;
	}

	@Override
	public int compareTo(PagerMessage o) {
		if (priority.ordinal() < o.priority.ordinal()) {
			return -1;
		} else if (priority.ordinal() > o.priority.ordinal()) {
			return 1;
		}

		// Same Priority, check Timestamp
		if (timestamp.isBefore(o.timestamp)) {
			return -1;
		} else if (timestamp.isAfter(o.timestamp)) {
			return 1;
		} else {
			return 0;
		}
	}

}