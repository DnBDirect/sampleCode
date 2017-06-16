package com.dnb.daas.match;

/**
 * HTTP Proxy Configuration Details
 */
public class ProxyConfig {

	private String host;
	private int port;
	private String username;
	private String password;

	public ProxyConfig(String host, int port, String username, String password) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}
