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

package de.rwth_aachen.afu.dapnet.legacy.transmitter_service.transmission;

import java.util.List;

import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.model.Call;

public interface PagerProtocol {
	List<PagerMessage> createMessagesFromCall(Call call);
}
