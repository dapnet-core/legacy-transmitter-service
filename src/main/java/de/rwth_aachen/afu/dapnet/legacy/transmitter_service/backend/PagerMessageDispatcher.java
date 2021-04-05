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

import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.transmission.PagerMessage;

/**
 * Message dispatcher interface.
 * 
 * @author Philipp Thiel
 */
@FunctionalInterface
public interface PagerMessageDispatcher {

	/**
	 * Dispatches a pager message.
	 * 
	 * @param message         Message to dispatch
	 * @param transmitterName Name of the destination transmitter
	 */
	void dispatchMessage(PagerMessage message, String transmitterName);

}
