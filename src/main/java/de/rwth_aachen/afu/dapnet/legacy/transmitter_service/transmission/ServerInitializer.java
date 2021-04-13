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

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * The server initializer initializes the newly created channel pipeline.
 * 
 * @author Philipp Thiel
 */
class ServerInitializer extends ChannelInitializer<SocketChannel> {
	private static final StringEncoder encoder = new StringEncoder(StandardCharsets.US_ASCII);
	private static final StringDecoder decoder = new StringDecoder(StandardCharsets.US_ASCII);
	private final MessageEncoder msgEncoder = new MessageEncoder();
	private final TransmitterManager manager;

	public ServerInitializer(TransmitterManager manager) {
		this.manager = Objects.requireNonNull(manager, "Transmitter manager must not be null.");
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();
		p.addLast(new DelimiterBasedFrameDecoder(2048, Delimiters.lineDelimiter()));
		p.addLast(decoder);
		p.addLast(encoder);
		p.addLast(msgEncoder);
		p.addLast(new ServerHandler(manager));
	}
}
