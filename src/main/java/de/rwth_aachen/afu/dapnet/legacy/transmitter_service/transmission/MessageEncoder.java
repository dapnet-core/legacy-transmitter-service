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

	private final int sendSpeed;

	public MessageEncoder(int sendSpeed) {
		this.sendSpeed = sendSpeed;
	}

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

		String encoded = String.format("#%02X %s:%X:%X:%s:%s\n", msg.getSequenceNumber(), type, sendSpeed,
				pm.getAddress(), pm.getSubAddress().getValue(), pm.getContent());

		out.add(encoded);
	}

}
