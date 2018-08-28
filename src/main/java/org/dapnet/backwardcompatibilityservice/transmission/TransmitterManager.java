package org.dapnet.backwardcompatibilityservice.transmission;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.backwardcompatibilityservice.model.Transmitter;
import org.dapnet.backwardcompatibilityservice.model.Transmitter.Status;
import org.dapnet.backwardcompatibilityservice.transmission.RabbitMQManager;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * This class manages connected transmitters.
 * 
 * @author Philipp Thiel
 */
public class TransmitterManager {
	private static final Logger logger = LogManager.getLogger();
	private final ConcurrentMap<String, TransmitterClient> connectedClients = new ConcurrentHashMap<>();
	private volatile TransmitterManagerListener listener;
    private volatile RabbitMQManager rabbitmqmanager;

    public TransmitterManager () {
        logger.info("Starting RabbitQM Manager");
        try {
            rabbitmqmanager = new RabbitMQManager("dapnet.calls", this);
        } catch (Exception e) {
        }
    }
	/**
	 * Gets a transmitter by its name.
	 * 
	 * @param name Transmitter name
	 * @return Transmitter or {@code null} if not found.
	 */
	public Transmitter getTransmitter(String name) {
		TransmitterManagerListener theListener = listener;
		if (theListener != null) {
			return theListener.handleGetTransmitter(name.toLowerCase());
		} else {
			return null;
		}
	}

	/**
	 * Sets the event listener.
	 * 
	 * @param listener Event listener instance.
	 */
	public void setListener(TransmitterManagerListener listener) {
		this.listener = listener;
	}

	/**
	 * Sends a message to all connected transmitters.
	 * 
	 * @param message Message to send.
	 */
	public void sendMessage(PagerMessage message) {
		connectedClients.values().forEach(c -> c.sendMessage(message));
	}

	/**
	 * Sends a message to a specific connected transmitter.
	 * 
	 * @param message         Message to send.
	 * @param transmitterName Transmitter name.
	 */
	public void sendMessage(PagerMessage message, String transmitterName) {
		TransmitterClient cl = connectedClients.get(transmitterName.toLowerCase());
		if (cl != null) {
			cl.sendMessage(message);
		}
	}

	/**
	 * Sends multiple messages to a specific connected transmitter.
	 * 
	 * @param messages        Messages to send.
	 * @param transmitterName Transmitter name.
	 */
	public void sendMessages(Collection<PagerMessage> messages, String transmitterName) {
		TransmitterClient cl = connectedClients.get(transmitterName.toLowerCase());
		if (cl != null) {
			cl.sendMessages(messages);
		}
	}


	/**
	 * Callback to handle connect events.
	 * 
	 * @param client Transmitter client to add.
	 */
	public void onConnect(TransmitterClient client) {
		Transmitter t = client.getTransmitter();
		if (t == null) {
			logger.warn("Client has no associated transmitter object.");
			client.close();
			return;
		}

		t.setStatus(Status.ONLINE);

		connectedClients.put(t.getName().toLowerCase(), client);

		notifyStatusChanged(listener, t);

		// Add RabbitMQ queue
        try {
            rabbitmqmanager.addRabbitMQQueue(t.getName());
        }
        catch (Exception e) {
        }
	}

	/**
	 * Callback that handles disconnect events.
	 * 
	 * @param client Transmitter to remove.
	 */
	public void onDisconnect(TransmitterClient client) {
		Transmitter t = client.getTransmitter();
		if (t == null) {
			return;
		}

		if (t.getStatus() != Status.ERROR) {
			t.setStatus(Status.OFFLINE);
		}
		// Pause RabbitMQ queue
		try {
			rabbitmqmanager.pauseRabbitMQQueue(t.getName());
		}
		catch (Exception e) {
		}

		connectedClients.remove(t.getName().toLowerCase());

		notifyStatusChanged(listener, t);
	}

	private static void notifyStatusChanged(TransmitterManagerListener listener, Transmitter t) {
		if (listener != null) {
			listener.handleTransmitterStatusChanged(t);
		}
	}

	/**
	 * Disconnects from all connected transmitters.
	 */
	public void disconnectFromAll() {
		connectedClients.values().forEach(cl -> cl.close());

		TransmitterManagerListener theListener = listener;
		if (theListener != null) {
			theListener.handleDisconnectedFromAllTransmitters();
		}
	}

	/**
	 * Disconnects from the given transmitter.
	 * 
	 * @param t Transmitter to disconnect from.
	 */
	public void disconnectFrom(Transmitter t) {
		TransmitterClient cl = connectedClients.remove(t.getName().toLowerCase());
		if (cl != null) {
			cl.close();
		}
	}
}
