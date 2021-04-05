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

package de.rwth_aachen.afu.dapnet.legacy.transmitter_service.backend;

/**
 * Interface for transmitter heartbeat services.
 * 
 * @author Philipp Thiel
 */
@FunctionalInterface
public interface TransmitterHeartbeatService {

	/**
	 * Posts the heartbeat for a transmitter.
	 * 
	 * @param callSign  Transmitter call sign.
	 * @param authKey   Transmitter authentication key
	 * @param ntpSynced NTP synced yes/no
	 * @return True if the heartbeat post request was successful
	 */
	boolean postHeartbeat(String callSign, String authKey, boolean ntpSynced);

}
