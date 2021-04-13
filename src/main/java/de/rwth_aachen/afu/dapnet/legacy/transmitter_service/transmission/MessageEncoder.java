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

import java.util.List;

import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.transmission.TransmitterClient.Message;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

/**
 * Encodes a {@link PagerMessage} into string.
 * 
 * @author Philipp Thiel
 */
@Sharable
class MessageEncoder extends MessageToMessageEncoder<Message> {

	public static final int MT_SYNCREQUEST = 2;
	public static final int MT_SYNCORDER = 3;
	public static final int MT_SLOTS = 4;
	public static final int MT_NUMERIC = 5;
	public static final int MT_ALPHANUM = 6;

	@Override
	protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
		final PagerMessage pm = msg.getMessage();

		// Mostly adapted from Sven Jung
		// See Diplomarbeit Jansen Page 30
		int type = 0;
		switch (pm.getContentType()) {
		case ALPHANUMERIC:
			type = MT_ALPHANUM;
			break;
		case NUMERIC:
			type = MT_NUMERIC;
			break;
		}

		String encoded = String.format("#%02X %s:%X:%X:%s:%s\n", msg.getSequenceNumber(), type,
				pm.getSendSpeed().getValue(), pm.getAddress(), pm.getSubAddress().getValue(), pm.getContent());

		out.add(encoded);
	}

}
