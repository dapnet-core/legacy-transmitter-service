package org.dapnet.backwardcompatibilityservice.transmission;

import java.io.StringReader;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.backwardcompatibilityservice.Settings;
import org.dapnet.backwardcompatibilityservice.model.Transmitter;
import org.dapnet.backwardcompatibilityservice.model.Transmitter.Status;
import org.dapnet.backwardcompatibilityservice.transmission.TransmissionSettings.PagingProtocolSettings;
import org.dapnet.backwardcompatibilityservice.transmission.TransmitterClient.AckType;
import org.glassfish.jersey.message.internal.MediaTypes;
import org.jgroups.stack.IpAddress;
import org.glassfish.json.JsonProviderImpl;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.ScheduledFuture;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import javax.json.*;
import javax.ws.rs.core.MediaType;

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
	private static final PagingProtocolSettings settings = Settings.getTransmissionSettings()
			.getPagingProtocolSettings();
	private static final int HANDSHAKE_TIMEOUT_SEC = 30;
	private final TransmitterManager manager;
	private ConnectionState state = ConnectionState.OFFLINE;
	private TransmitterClient client;
	private ChannelPromise handshakePromise;
	private SyncTimeHandler syncHandler;

	private static final String BOOTSTRAP_URL = "http://dapnetdc2.db0sda.ampr.org/transmitters/_bootstrap";
	private static final String HEARTBEAT_URL = "http://dapnetdc2.db0sda.ampr.org/transmitters/_heartbeat";

	public ServerHandler(TransmitterManager manager) {
		this.manager = manager;
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

		syncHandler = new SyncTimeHandler(settings.getNumberOfSyncLoops());

		handshakePromise = ctx.newPromise();
		initHandshakeTimeout(ctx);

		state = ConnectionState.AUTH_PENDING;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.info("Connection closed.");

		if (handshakePromise != null) {
			handshakePromise.trySuccess();
		}

		if (client != null) {
			int count = client.getPendingMessageCount();
			if (count > 0) {
				logger.warn("Client has {} pending messages.", count);
			}

			manager.onDisconnect(client);
		}

		state = ConnectionState.OFFLINE;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		state = ConnectionState.EXCEPTION_CAUGHT;

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

		ctx.close();
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

		Transmitter t = client.getTransmitter();
		Client BootstrapPOSTClient = Client.create();
		WebResource webResource = BootstrapPOSTClient.resource(HEARTBEAT_URL);
		JsonObjectBuilder POSTRequest = Json.createObjectBuilder();
		POSTRequest.add("callsign", t.getName());
		POSTRequest.add("auth_key", t.getAuthKey());
		POSTRequest.add("ntp_synced", true);

		String POSTJSONString = POSTRequest.toString();
		ClientResponse POSTrepsonse = webResource.type("application/json").post(ClientResponse.class,POSTJSONString);
		if (POSTrepsonse.getType() != MediaType.APPLICATION_JSON_TYPE) {
			logger.error("Invalid Media Type from Heartbeat Service: " + POSTrepsonse.getType().toString() +
					"while trying to heartbeat transmitter " + t.getName());
			return;
		}

		String POSTresponseJOSN = POSTrepsonse.getEntity(String.class);

		if (POSTrepsonse.getStatus() != 200) {
			logger.error("Heartbeat Service returned non expected status code: " +
					Integer.toString(POSTrepsonse.getStatus()) +
					"instead of 200 while trying to heartbeat transmitter " + t.getName());
		}
	}

	private void handleAuth(ChannelHandlerContext ctx, String msg) throws Exception {
		Matcher authMatcher = AUTH_PATTERN.matcher(msg);
		if (!authMatcher.matches()) {
                        logger.error("Invalid welcome message format: " + msg);
                        ctx.writeAndFlush("07 Invalid welcome message format").addListener(ChannelFutureListener.CLOSE);
                        return;
		}

		String type = authMatcher.group(1);
		String version = authMatcher.group(2);
		String name = authMatcher.group(3);
		String auth_key = authMatcher.group(4);

		// Open connection to Transmitter Bootstrap service and check credentials and get timeslots

		Client BootstrapPOSTClient = Client.create();
		WebResource webResource = BootstrapPOSTClient.resource(BOOTSTRAP_URL);
        JsonObjectBuilder POSTRequest = Json.createObjectBuilder();
        POSTRequest.add("callsign", name);
        POSTRequest.add("auth_key", auth_key);
        JsonObjectBuilder POSTRequest_software = Json.createObjectBuilder();
        POSTRequest_software.add("name", type);
        POSTRequest_software.add("version", version);
        POSTRequest.add("software", POSTRequest_software);

        String POSTJSONString = POSTRequest.toString();
        ClientResponse POSTrepsonse = webResource.type("application/json").post(ClientResponse.class,POSTJSONString);
        if (POSTrepsonse.getType() != MediaType.APPLICATION_JSON_TYPE) {
            logger.error("Invalid Media Type from Bootstrap Service: " + POSTrepsonse.getType().toString() +
                    "while trying to register transmitter " + name);
            ctx.writeAndFlush("07 Invalid Media Type from Bootstrap Service, sorry not your fault.").addListener(ChannelFutureListener.CLOSE);
            return;
        }

        String POSTresponseJOSN = POSTrepsonse.getEntity(String.class);

        JsonReader jsonReader = Json.createReader(new StringReader(POSTresponseJOSN));
        JsonObject POSTJSONresponseObject = jsonReader.readObject();
        jsonReader.close();

        switch (POSTrepsonse.getStatus()) {
            case 432 : {
                // Locked
                logger.error("Your transmitter is not allowed to connect due to: " +
                        POSTJSONresponseObject.getString("error") +
                        "while trying to register transmitter " + name);
                ctx.writeAndFlush("07 Invalid Media Type from Bootstrap Service, sorry not your fault.").addListener(ChannelFutureListener.CLOSE);
                return;
            }

            case 401: case 403: {
                // Unauthorized or Forbidden
                logger.error("Your transmitter is not allowed to connect due to: " +
                        POSTJSONresponseObject.getString("error") +
                        "while trying to register transmitter " + name);
                ctx.writeAndFlush("07 Your transmitter is not allowed to connect. Check callsign and auth_key").addListener(ChannelFutureListener.CLOSE);
                return;
            }

            case 200: case 201: {
                // Created
                JsonArray Timeslotsarray = POSTJSONresponseObject.getJsonArray("tileslots");
                // Build Transmitter
                Transmitter t = new Transmitter();
                t.setName(name);
                t.setAuthKey(auth_key);
                t.setNodeName("db0sda-dc2");
                t.setDeviceType(type);
                t.setDeviceVersion(version);
                t.setStatus(Status.ONLINE);

                // Convert JSON boolean array to
                String TimeslotsString = "";
                for (int i = 0; i < Timeslotsarray.size(); i++) {
                    if (Timeslotsarray.getBoolean(i)) {
                        TimeslotsString = TimeslotsString + Integer.toString(i).toUpperCase();
                    }
                }
                t.setTimeSlot(TimeslotsString);

                // Close existing connection if necessary. This is a no-op if the
                // transmitter is not connected.
                manager.disconnectFrom(t);

                t.setDeviceType(type);
                t.setDeviceVersion(version);
                t.setAddress(new IpAddress((InetSocketAddress) ctx.channel().remoteAddress()));

                client.setTransmitter(t);

                // Begin the sync time procedure
                syncHandler.handleMessage(ctx, msg);

                state = ConnectionState.SYNC_TIME;
                return;
            }
            default: {
                logger.error("Error communicating with Bootstrap Service due to : " +
                        POSTJSONresponseObject.getString("error") +
                        "while trying to register transmitter " + name);
                ctx.writeAndFlush("07 Error communicating with Bootstrap Service, sorry not your fault.").addListener(ChannelFutureListener.CLOSE);
                return;
            }

        }
	}

	private void handleSyncTime(ChannelHandlerContext ctx, String message) throws Exception {
		syncHandler.handleMessage(ctx, message);

		if (syncHandler.isDone()) {
			syncHandler = null;

			// Send timeslots to client
			Transmitter t = client.getTransmitter();
			String msg = String.format("%d:%s\n", MessageEncoder.MT_SLOTS, t.getTimeSlot());
			ctx.writeAndFlush(msg);

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
}
