package de.rwth_aachen.afu.dapnet.legacy.transmitter_service.transmission;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.backend.ServiceError;
import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.backend.ServiceResult;
import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.backend.TransmitterBootstrapService;
import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.backend.TransmitterHeartbeatService;
import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.transmission.Transmitter.Status;
import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.transmission.TransmitterClient.AckType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.ScheduledFuture;

class ServerHandler extends SimpleChannelInboundHandler<String> {

	private enum ConnectionState {
		AUTH_PENDING, SYNC_TIME, TIMESLOTS_SENT, ONLINE, OFFLINE, EXCEPTION_CAUGHT
	}

	private static final Logger logger = LogManager.getLogger();
	// Ack message #04 +
	private static final Pattern ACK_PATTERN = Pattern.compile("#(\\p{XDigit}{2}) ([-%\\+])");
	// Welcome string [RasPager v1.0-SCP-#2345678 abcde]
	private static final Pattern AUTH_PATTERN = Pattern
			.compile("\\[([/\\-\\p{Alnum}]+) v(\\d[\\d\\.]+[\\p{Graph}]*) ([\\p{Alnum}_]+) (\\p{Alnum}+)\\]");
	private static final int HANDSHAKE_TIMEOUT_SEC = 30;
	private static final int CLOSE_TASK_DELAY_SEC = 5;
	private final TransmitterManager manager;
	private ConnectionState state = ConnectionState.OFFLINE;
	private TransmitterClient client;
	private ChannelPromise handshakePromise;
	private SyncTimeHandler syncHandler;

	public ServerHandler(TransmitterManager manager) {
		this.manager = Objects.requireNonNull(manager, "Transmitter manager must not be null.");
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		switch (state) {
		case AUTH_PENDING:
			handleAuth(ctx, msg);
			break;
		case SYNC_TIME:
			handleSyncTime(ctx, msg);
			break;
		case TIMESLOTS_SENT:
			handleTimeslotsAck(ctx, msg);
			break;
		case ONLINE:
			handleMessageAck(msg);
			sendHearbeat(ctx);
			break;
		default:
			logger.fatal("Invalid state.");
			ctx.close();
			break;
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.info("Accepted new connection from {}", ctx.channel().remoteAddress());

		// Do not add the client to the transmitter manager yet. This is done
		// once the handshake is finished.
		client = new TransmitterClient(ctx.channel());

		syncHandler = new SyncTimeHandler(manager.getConfiguration().getNumberOfSyncLoops());

		handshakePromise = ctx.newPromise();
		initHandshakeTimeout(ctx);

		state = ConnectionState.AUTH_PENDING;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (client != null) {
			logger.info("Connection to {} with callsign {} closed in state {} using {} {}.",
					ctx.channel().remoteAddress(), client.getName(), state, client.getDeviceType(),
					client.getDeviceVersion());
		} else {
			logger.info("Connection to {} closed in state {}.", ctx.channel().remoteAddress(), state);
		}

		if (handshakePromise != null) {
			handshakePromise.trySuccess();
		}

		if (client != null) {
			int count = client.getPendingMessageCount();
			if (count > 0) {
				logger.warn("Client {} has {} pending messages.", client.getName(), count);
			}

			manager.onDisconnect(client);
		}

		state = ConnectionState.OFFLINE;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		state = ConnectionState.EXCEPTION_CAUGHT;

		try {
			String transmitterName = null;
			if (client != null) {
				Transmitter t = client.getTransmitter();
				if (t != null) {
					t.setStatus(Status.ERROR);
					transmitterName = t.getName();
				}
			}

			if (transmitterName != null && !transmitterName.isEmpty()) {
				if (cause instanceof TransmitterException) {
					logger.error("Closing connection for {}: {}", transmitterName, cause.getMessage());
				} else {
					logger.error("Exception in server handler for {}.", transmitterName, cause);
				}
			} else {
				if (cause instanceof TransmitterException) {
					logger.error("Closing connection: {}", cause.getMessage());
				} else {
					logger.error("Exception in server handler.", cause);
				}
			}
		} catch (Exception ex) {
			logger.error("Exception in exception handler", ex);
		} finally {
			// Delay the channel close
			Runnable closeTask = new SendAndCloseTask(ctx.channel());
			ctx.executor().schedule(closeTask, CLOSE_TASK_DELAY_SEC, TimeUnit.SECONDS);
		}
	}

	private void handleMessageAck(String msg) throws Exception {
		Matcher ackMatcher = ACK_PATTERN.matcher(msg);
		if (!ackMatcher.matches()) {
			throw new TransmitterException("Invalid response received: " + msg);
		}

		int seq = Integer.parseInt(ackMatcher.group(1), 16);
		AckType type = AckType.ERROR;
		switch (ackMatcher.group(2)) {
		case "+":
			type = AckType.OK;
			break;
		case "%":
			type = AckType.RETRY;
			break;
		case "-":
			type = AckType.ERROR;
		}

		if (!client.ackMessage(seq, type)) {
			Transmitter t = client.getTransmitter();
			if (t != null) {
				logger.warn("Invalid ack received from {}: {}", t.getName(), msg);
			} else {
				logger.warn("Invalid ack received: {}", msg);
			}
		}
	}

	/**
	 * Sends the transmitter heartbeat.
	 * 
	 * @param ctx Channel handler context
	 */
	private void sendHearbeat(ChannelHandlerContext ctx) {
		final Transmitter t = client.getTransmitter();
		if (t == null) {
			// This should not happen at this stage...
			logger.warn("Could not send heartbeat, transmitter is null.");
			return;
		}

		logger.debug("Sending heartbeat for transmitter '{}'", t.getName());

		final TransmitterHeartbeatService service = manager.getHearbeatService();
		if (service == null) {
			logger.debug("No heartbeat service, skipping.");
			return;
		}

		Runnable heartbeat = () -> {
			try {
				service.postHeartbeat(t.getName(), t.getAuthKey(), true);
			} catch (Exception ex) {
				logger.error("Failed to send transmitter heartbeat.", ex);
			}
		};

		ctx.executor().execute(heartbeat);
	}

	private void handleAuth(ChannelHandlerContext ctx, String msg) throws Exception {
		Matcher authMatcher = AUTH_PATTERN.matcher(msg);
		if (!authMatcher.matches()) {
			logger.error("Invalid welcome message format: " + msg);
			SendAndCloseTask task = new SendAndCloseTask(ctx.channel(), "7 Invalid welcome message format");
			ctx.executor().schedule(task, CLOSE_TASK_DELAY_SEC, TimeUnit.SECONDS);
			return;
		}

		String type = authMatcher.group(1);
		String version = authMatcher.group(2);
		String name = authMatcher.group(3);
		String key = authMatcher.group(4);

		final Transmitter transmitter = bootstrapTransmitter(ctx, name, key, type, version);
		if (transmitter == null) {
			// Logging and sending the response is done by the bootstrap handler
			return;
		}

		// Close existing connection if necessary. This is a no-op if the transmitter is
		// not connected.
		manager.disconnectFrom(transmitter);

		client.setTransmitter(transmitter);

		// Begin the sync time procedure
		syncHandler.handleMessage(ctx, msg);

		state = ConnectionState.SYNC_TIME;
	}

	private Transmitter bootstrapTransmitter(ChannelHandlerContext ctx, String callSign, String authKey, String type,
			String version) {
		final TransmitterBootstrapService service = manager.getBootstrapService();
		if (service == null) {
			logger.error("Bootstrap service is null.");
			return null;
		}

		ServiceResult<String> result = service.postBootstrapRequest(callSign, authKey, type, version);
		if (result.isOk()) {
			Transmitter t = new Transmitter();
			t.setName(callSign);
			t.setAuthKey(authKey);
			t.setNodeName("db0sda-dc2");
			t.setDeviceType(type);
			t.setDeviceVersion(version);
			t.setStatus(Status.ONLINE);
			t.setTimeSlot(result.getValue());
			t.setAddress((InetSocketAddress) ctx.channel().remoteAddress());

			return t;
		} else {
			String responseMessage;
			final ServiceError error = result.getError();
			switch (error.getStatusCode()) {
			case 432:
				// Locked
				logger.error("Transmitter '{}' is not allowed to connect: {}", callSign, error.getErrorMessage());
				responseMessage = "7 Transmitter is not allowed to connect: " + error.getErrorMessage();
				break;

			case 401:
			case 403:
				// Unauthorized or Forbidden
				logger.error("Transmitter '{}' unauthorized or forbidden: {}", callSign, error.getErrorMessage());
				responseMessage = "7 Transmitter unauthorized or forbidden: " + error.getErrorMessage();
				break;

			default:
				responseMessage = "7 Bootstrap error: " + error.getErrorMessage();
				break;
			}

			SendAndCloseTask task = new SendAndCloseTask(ctx.channel(), responseMessage);
			ctx.executor().schedule(task, CLOSE_TASK_DELAY_SEC, TimeUnit.SECONDS);

			return null;
		}
	}

	private void handleSyncTime(ChannelHandlerContext ctx, String message) throws Exception {
		syncHandler.handleMessage(ctx, message);

		if (syncHandler.isDone()) {
			syncHandler = null;

			// Send timeslots to client
			Transmitter t = client.getTransmitter();
			if (t != null) {
				String msg = String.format("%d:%s\n", MessageEncoder.MT_SLOTS, t.getTimeSlot());

				ctx.writeAndFlush(msg);
			} else {
				logger.error("Transmitter is null, cannot send time slots.");
				ctx.close();
			}

			state = ConnectionState.TIMESLOTS_SENT;
		}
	}

	private void handleTimeslotsAck(ChannelHandlerContext ctx, String msg) throws Exception {
		if (!msg.equals("+")) {
			throw new TransmitterException("Wrong ack received.");
		}

		handshakePromise.trySuccess();
		handshakePromise = null;

		// Now it is time to inform the transmitter manager of the new client
		manager.onConnect(client);

		state = ConnectionState.ONLINE;
	}

	private void initHandshakeTimeout(final ChannelHandlerContext ctx) {
		final ChannelPromise p = handshakePromise;

		final ScheduledFuture<?> timeoutFuture = ctx.executor().schedule(() -> {
			if (!p.isDone()) {
				logger.warn("Handshake timed out.");
				ctx.flush();
				ctx.close();
			}
		}, HANDSHAKE_TIMEOUT_SEC, TimeUnit.SECONDS);

		p.addListener((f) -> {
			timeoutFuture.cancel(false);
		});
	}

	private static class SendAndCloseTask implements Runnable {
		private final Channel channel;
		private final String message;

		public SendAndCloseTask(Channel channel) {
			this(channel, null);
		}

		public SendAndCloseTask(Channel channel, String message) {
			this.channel = channel;
			this.message = message;
		}

		@Override
		public void run() {
			if (channel == null) {
				return;
			}

			if (message != null) {
				channel.writeAndFlush(message).addListener(ChannelFutureListener.CLOSE);
			} else {
				channel.close();
			}
		}
	}
}
