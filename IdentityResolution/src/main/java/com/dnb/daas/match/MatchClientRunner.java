package com.dnb.daas.match;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/**
 * This is an example class to demonstrate a call to the D&B MatchPlus API to obtain an access token and subsequent calls to
 * make a Name and Address Lookup request for company Dun & Bradstreet in Short Hills, New Jersey, US.
 *
 * You will need to set the Match Host Server, Consumer Key and Consumer Secret that have been provided to you by D&B.
 *
 * If you are required to route your requests through a proxy server, then provide the configuration details to allow
 * the requests to be routed.
 *
 * This example depends on some open-source Java libraries that must be obtained separately. See accompanying
 * documentation for details.
 *
 */
public class MatchClientRunner {

    // change the following values below
    private static final String CONSUMER_KEY = "yourConsumerKey";
    private static final String CONSUMER_SECRET = "yourConsumerSecret";
    private static final String PROXY_HOST = "proxyHost";
    private static final Integer PROXY_PORT = 3128;
    private static final String PROXY_USERNAME = "username";
    private static final String PROXY_PASSWORD = "password";


    public static void main(String[] args) {
        try {
            String host = "plus.dnb.com";

            // Optional: If you need to route through a proxy server, then provide proxy details
            // Parameters are host, port (both mandatory), username, password (both optional)
            // If proxy is not needed, comment out this line and remove reference from the
            // MatchClientExample constructor call below
            ProxyConfig proxyConfig = new ProxyConfig(PROXY_HOST, PROXY_PORT, PROXY_USERNAME, PROXY_PASSWORD);
            // Construct the EdgeClientExample instance
            MatchClientExample client = new MatchClientExample(host, proxyConfig);

            // Obtain an accessToken using your consumerKey and consumerSecret
            String accessToken = client.getAccessToken(CONSUMER_KEY, CONSUMER_SECRET);

            // Set up parameters
            List<NameValuePair> parameters = new ArrayList<NameValuePair>();
            // Name & Address Lookup parameters
             parameters.add(new BasicNameValuePair("name", "Dun & Bradstreet"));
             parameters.add(new BasicNameValuePair("countryISOAlpha2Code", "US"));
             parameters.add(new BasicNameValuePair("addressLocality", "Short Hills"));
             parameters.add(new BasicNameValuePair("addressRegion", "NJ"));

            // Duns Lookup parameter
            // parameters.add(new BasicNameValuePair("duns", "884114609"));

            String result = client.cleanseMatch(parameters, accessToken);

            System.out.println(result);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
