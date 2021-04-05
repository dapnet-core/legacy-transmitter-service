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

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class Transmitter implements NamedObject {

	public enum Status {
		OFFLINE, ONLINE, ERROR, DISABLED
	}

	private String name;
	private String authKey;
	private String nodeName;
	private InetSocketAddress address;
	private String timeSlot;
	private String deviceType;
	private String deviceVersion;
	private AtomicLong messageCount = new AtomicLong();
	private Status status;
	private Instant lastUpdate;
	private Instant lastConnected;
	private Instant connectedSince;

	public Transmitter() {
	}

	public Transmitter(Transmitter other) {
		if (other == null) {
			throw new NullPointerException("Other object must not be null.");
		}

		name = other.name;
		authKey = other.authKey;
		nodeName = other.nodeName;

		if (other.address != null) {
			address = new InetSocketAddress(other.address.getAddress(), other.address.getPort());
		} else {
			address = null;
		}

		timeSlot = other.timeSlot;
		deviceType = other.deviceType;
		deviceVersion = other.deviceVersion;
		messageCount = new AtomicLong(other.messageCount.get());
		status = other.status;
		lastUpdate = other.lastUpdate;
		lastConnected = other.lastConnected;
		connectedSince = other.connectedSince;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthKey() {
		return authKey;
	}

	public void setAuthKey(String authKey) {
		this.authKey = authKey;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public InetSocketAddress getAddress() {
		return address;
	}

	public void setAddress(InetSocketAddress address) {
		this.address = address;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getDeviceVersion() {
		return deviceVersion;
	}

	public void setDeviceVersion(String deviceVersion) {
		this.deviceVersion = deviceVersion;
	}

	public String getTimeSlot() {
		return timeSlot;
	}

	public void setTimeSlot(String timeSlot) {
		this.timeSlot = timeSlot;
	}

	/**
	 * Gets the transmitter status.
	 * 
	 * @return Transmitter status.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Sets the transmitter status.
	 * 
	 * @param status Transmitter status.
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * Gets the timepoint when the transmitter connected.
	 * 
	 * @return Timepoint
	 */
	public Instant getLastConnected() {
		return lastConnected;
	}

	/**
	 * Sets the timepoint when the transmitter connected.
	 * 
	 * @param when Timepoint
	 */
	public void setLastConnected(Instant when) {
		this.lastConnected = when;
	}

	/**
	 * Gets the timepoint since when the transmitter is connected.
	 * 
	 * @return Timepoint
	 */
	public Instant getConnectedSince() {
		return connectedSince;
	}

	/**
	 * Sets the timepoint since when the transmitter is connected.
	 * 
	 * @param since Timepoint
	 */
	public void setConnectedSince(Instant since) {
		this.connectedSince = since;
	}

	/**
	 * Sets the last update timestamp.
	 * 
	 * @param when Last update timestamp
	 */
	public void setLastUpdate(Instant when) {
		this.lastUpdate = when;
	}

	/**
	 * Gets the last update timestamp.
	 * 
	 * @return Last update timestamp
	 */
	public Instant getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * Gets the number of messages sent to this transmitter.
	 * 
	 * @return Message count
	 */
	public long getMessageCount() {
		return messageCount.get();
	}

	/**
	 * Sets the number of messages sent to this transmitter.
	 * 
	 * @param messageCount Message count
	 */
	public void setMessageCount(long messageCount) {
		this.messageCount.set(messageCount);
	}

	/**
	 * Atomically updates the message counter.
	 * 
	 * @param delta Delta to add or subtract.
	 */
	public void updateMessageCount(long delta) {
		messageCount.addAndGet(delta);
	}

	@Override
	public String toString() {
		return String.format("Transmitter{name='%s', status=%s}", name, status);
	}

}