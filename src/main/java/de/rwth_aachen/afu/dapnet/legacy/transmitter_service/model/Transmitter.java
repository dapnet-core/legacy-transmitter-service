/*
 * DAPNET CORE PROJECT
 * Copyright (C) 2016
 *
 * Daniel Sialkowski
 *
 * daniel.sialkowski@rwth-aachen.de
 *
 * Institute of High Frequency Technology
 * RWTH AACHEN UNIVERSITY
 * Melatener Str. 25
 * 52074 Aachen
 */

package de.rwth_aachen.afu.dapnet.legacy.transmitter_service.model;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Collection;

import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.model.validator.TimeSlot;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class Transmitter implements Serializable, Searchable {

	private static final long serialVersionUID = -8142160974834002456L;

	@NotNull
	@Size(min = 3, max = 20)
	private String name;

	@NotNull
	@Size(min = 1, max = 64)
	private String authKey;

	private String nodeName;

	private InetSocketAddress address;

	@NotNull
	@TimeSlot()
	private String timeSlot;

	@NotNull
	@Size(min = 1, message = "must contain at least one ownerName")
	private Collection<String> ownerNames;

	private String deviceType;

	private String deviceVersion;

	public enum Status {
		OFFLINE, ONLINE, ERROR, DISABLED
	}

	@NotNull
	private Status status;

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

	@Override
	public String toString() {
		return String.format("Transmitter{name='%s', status=%s}", name, status);
	}
}