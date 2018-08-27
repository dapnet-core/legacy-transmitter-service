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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;

public class TransmissionManager {
	private static final Logger logger = LogManager.getLogger();
	private final PagerProtocol protocol = new SkyperProtocol();
	private final TransmitterManager transmitterManager = new TransmitterManager();


/*	public void handleNews(News news) {
		try {
			PagerMessage message = protocol.createMessageFromNews(news);
			transmitterManager.sendMessage(message, news.getRubric().getTransmitterGroups());

			logger.info("News sent to transmitters.");
		} catch (Exception e) {
			logger.error("Failed to send News", e);
		}
	}

	public void handleNewsAsCall(News news) {
		try {
			PagerMessage message = protocol.createMessageFromNewsAsCall(news);
			transmitterManager.sendMessage(message, news.getRubric().getTransmitterGroups());

			logger.info("News sent to transmitters as call.");
		} catch (Exception ex) {
			logger.error("Failed to send News as call", ex);
		}
	}

	public void handleRubric(Rubric rubric) {
		try {
			PagerMessage message = protocol.createMessageFromRubric(rubric);
			transmitterManager.sendMessage(message, rubric.getTransmitterGroups());

			logger.info("Rubric {} sent to transmitters.", rubric.getName());
		} catch (Exception e) {
			logger.error("Failed to send Rubric " + rubric.getName(), e);
		}
	}

	public void handleRubricToTransmitter(Rubric rubric, String transmitterName) {
		try {
			PagerMessage message = protocol.createMessageFromRubric(rubric);
			transmitterManager.sendMessage(message, transmitterName);

			logger.info("Rubric {} sent to transmitter {}", rubric.getName(), transmitterName);
		} catch (Exception ex) {
			logger.error("Failed to send rubric " + rubric.getName() + " to transmitter " + transmitterName, ex);
		}
	}

	public void handleCall(Call call) {
		try {
			List<PagerMessage> messages = protocol.createMessagesFromCall(call);
			transmitterManager.sendMessages(messages, call.getTransmitterGroups());

			logger.info("Call sent to {} CallSigns, to {} Pagers, using {} TransmitterGroups.",
					call.getCallSigns().size(), messages.size(), call.getTransmitterGroupNames().size());

			// XXX No other easy way of doing this without performing a
			// cluster-wide remote procedure call
			Set<Transmitter> transmitters = new HashSet<>();
			for (TransmitterGroup grp : call.getTransmitterGroups()) {
				transmitters.addAll(grp.getTransmitters());
			}

			transmitters.forEach(t -> t.updateCallCount(1));
		} catch (Exception e) {
			logger.error("Failed to send Call", e);
		}
	}
*/

	public TransmitterManager getTransmitterManager() {
		return transmitterManager;
	}
}
