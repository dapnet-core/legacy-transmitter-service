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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.model.validator.ValidName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

public class CallSign implements Serializable, Searchable {
	private static final long serialVersionUID = 1884808852367562476L;
	private static volatile State state;

	// ID
	@NotNull
	@Size(min = 3, max = 20)
	private String name;

	@NotNull
	@Size(min = 0, max = 60)
	private String description;

	@NotNull
	private boolean numeric = false;

	@NotNull
	@Size(min = 1, message = "must contain at least one ownerName")
	private Collection<String> ownerNames;

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isNumeric() {
		return numeric;
	}

	public void setNumeric(boolean numeric) {
		this.numeric = numeric;
	}

	public static void setState(State statePar) {
		state = statePar;
	}

	@ValidName(message = "must contain names of existing users", fieldName = "ownerNames", constraintName = "ValidOwnerNames")
	public Collection<User> getOwners() throws Exception {
		if (ownerNames == null) {
			return null;
		}
		if (state == null) {
			throw new Exception("StateNotSetException");
		}

		ConcurrentMap<String, User> users = state.getUsers();
		ArrayList<User> results = new ArrayList<>();
		for (String owner : ownerNames) {
			User u = users.get(owner.toLowerCase());
			if (u != null)
				results.add(u);
		}

		if (results.size() == ownerNames.size()) {
			return results;
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return String.format("CallSign{name='%s'}", name);
	}
}
