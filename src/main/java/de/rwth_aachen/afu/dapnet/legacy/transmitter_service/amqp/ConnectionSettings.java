package de.rwth_aachen.afu.dapnet.legacy.transmitter_service.amqp;

/**
 * Connection settings for AMQP servers.
 * 
 * @author Philipp Thiel
 */
public class ConnectionSettings {

	private String host;
	private String user;
	private String password;
	private String exchange;

	/**
	 * Gets the host name.
	 * 
	 * @return Host name
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Sets the host name.
	 * 
	 * @param host Host name
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Gets the user name.
	 * 
	 * @return User name
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Sets the user name.
	 * 
	 * @param user User name
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Gets the password.
	 * 
	 * @return Password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password.
	 * 
	 * @param password Password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Gets the exchange name.
	 * 
	 * @return Exchange name
	 */
	public String getExchange() {
		return exchange;
	}

	/**
	 * Sets the exchange name.
	 * 
	 * @param exchange Exchange name
	 */
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

}
