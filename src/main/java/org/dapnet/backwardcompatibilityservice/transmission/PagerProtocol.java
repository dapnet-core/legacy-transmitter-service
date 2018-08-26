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

package org.dapnet.backwardcompatibilityservice.transmission;

import org.dapnet.backwardcompatibilityservice.model.Call;

import java.time.LocalDateTime;
import java.util.List;

public interface PagerProtocol {
	PagerMessage createMessageFromTime(LocalDateTime time);

	PagerMessage createMessageFromTimeSwissphone(LocalDateTime time);
	List<PagerMessage> createMessagesFromCall(Call call);
}
