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
import org.dapnet.backwardcompatibilityservice.Settings;
import org.dapnet.backwardcompatibilityservice.model.Call;
import org.dapnet.backwardcompatibilityservice.transmission.PagerMessage.MessagePriority;
import org.dapnet.backwardcompatibilityservice.transmission.TransmissionSettings.PagingProtocolSettings;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkyperProtocol implements PagerProtocol {
	private static final PagingProtocolSettings settings = Settings.getTransmissionSettings()
			.getPagingProtocolSettings();
	private static final Logger logger = LogManager.getLogger();
	private static final Pattern NUMERIC_PATTERN = Pattern.compile("[-Uu\\d\\(\\) ]+");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HHmmss   ddMMyy");
	private static final DateTimeFormatter DATE_FORMATTER_SWISSPHONE = DateTimeFormatter
			.ofPattern("'XTIME='HHmmddMMyy");
	private static final Charset PAGER_CHARSET = new DE_ASCII7();

	@Override
	public List<PagerMessage> createMessagesFromCall(Call call) {

		MessagePriority priority = call.isEmergency() ? MessagePriority.EMERGENCY : MessagePriority.CALL;
		Instant now = Instant.now();

		try {
			// Test if message is numeric
			Matcher m = NUMERIC_PATTERN.matcher(call.getText());
			boolean numeric = m.matches();

			List<PagerMessage> messages = new ArrayList<>();
/*
			for (CallSign callsign : call.getCallSigns()) {
				FunctionalBits mode;
				String text;
				if (!callsign.isNumeric()) {
					// Support for alphanumeric messages -> create ALPHANUM
					// message
					mode = FunctionalBits.ALPHANUM;
					text = encodeString(call.getText());
				} else if (numeric) {
					// No support for alphanumeric messages but text is numeric
					// -> create NUMERIC message
					mode = FunctionalBits.NUMERIC;
					text = call.getText().toUpperCase();
				} else {
					// No support for alphanumeric messages and non-numeric
					// message -> skip
					logger.warn("Callsign {} does not support alphanumeric messages.", callsign.getName());
					continue;
				}
*/
/*
				for (Pager pager : callsign.getPagers()) {
					messages.add(new PagerMessage(now, text, pager.getNumber(), priority, mode));
				}
			}
*/
			return messages;
		} catch (Exception ex) {
			logger.error("Failed to create messages from call.", ex);
			return null;
		}
	}


	@Override
	public PagerMessage createMessageFromTime(LocalDateTime time) {
		return new PagerMessage(DATE_FORMATTER.format(time), 2504, PagerMessage.MessagePriority.TIME,
				PagerMessage.FunctionalBits.NUMERIC);
	}

	@Override
	public PagerMessage createMessageFromTimeSwissphone(LocalDateTime time) {
		String s = DATE_FORMATTER_SWISSPHONE.format(time);
		String s2 = s + s;
		return new PagerMessage(s2, 165856, PagerMessage.MessagePriority.TIME, PagerMessage.FunctionalBits.ALPHANUM);
	}


	private static String encodeString(String input) {
		if (input != null) {
			byte[] encoded = input.getBytes(PAGER_CHARSET);
			return new String(encoded, StandardCharsets.US_ASCII);
		} else {
			return null;
		}
	}
}
