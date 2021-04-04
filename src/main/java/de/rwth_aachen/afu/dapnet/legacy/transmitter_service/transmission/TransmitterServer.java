package de.rwth_aachen.afu.dapnet.legacy.transmitter_service.transmission;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth_aachen.afu.dapnet.legacy.transmitter_service.CoreStartupException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * This class implements the transmitter server.
 * 
 * @author Philipp Thiel
 */
public class TransmitterServer {

	private static final Logger LOGGER = LogManager.getLogger();
	private final TransmitterManager manager;
	private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	private final EventLoopGroup workerGroup = new NioEventLoopGroup();

	/**
	 * Constructs a new object instance.
	 * 
	 * @param manager Transmitter manager to use
	 */
	public TransmitterServer(TransmitterManager manager) {
		this.manager = Objects.requireNonNull(manager, "Transmitter manager must not be null.");
	}

	/**
	 * Starts the transmitter server.
	 * 
	 * @throws CoreStartupException if the server startup fails
	 */
	public void start() {
		try {
			int port = manager.getConfiguration().getServerPort();

			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup);
			b.channel(NioServerSocketChannel.class);
			b.childHandler(new ServerInitializer(manager));
			b.childOption(ChannelOption.SO_KEEPALIVE, true);
			b.bind(port).sync();

			LOGGER.info("Server started on port: {}", port);
		} catch (Exception ex) {
			// LOGGER.fatal("Failed to start the server.", ex);
			throw new CoreStartupException(ex);
		}
	}

	/**
	 * Stops the transmitter server.
	 */
	public void shutdown() {
		try {
			bossGroup.shutdownGracefully().sync();
		} catch (Exception e) {
			LOGGER.warn("Failed to shut down boss group.", e);
		}

		try {
			workerGroup.shutdownGracefully().sync();
		} catch (Exception e) {
			LOGGER.warn("Failed to shut down worker group.", e);
		}

		LOGGER.info("Server stopped.");
	}

}
