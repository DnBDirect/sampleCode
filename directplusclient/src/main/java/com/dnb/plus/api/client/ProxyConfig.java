package com.dnb.plus.api.client;

/**
 * HTTP proxy configuration details.
 */
public class ProxyConfig {

    /** The host name of the proxy server. */
    private String host;

    /** The port number of the proxy server. */
    private int port;

    /** The user name to be authenticated by the proxy server. */
    private String username;

    /** The password name to be authenticated by the proxy server. */
    private String password;

    /**
     * Proxy configuration constructor.
     *
     * @param host (required) the host name of the proxy server
     * @param port (required) the port number of the proxy server
     * @param username (optional) the user name to be authenticated by the proxy server
     * @param password (optional) the password name to be authenticated by the proxy server
     */
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
