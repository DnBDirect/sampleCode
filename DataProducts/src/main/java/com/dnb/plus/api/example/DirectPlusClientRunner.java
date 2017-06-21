package com.dnb.plus.api.example;

import com.dnb.plus.api.client.DirectPlusClient;
import com.dnb.plus.api.client.ProxyConfig;

/**
 * This is an example class to demonstrate a call to the D&B Direct Plus API to obtain an access token and subsequent
 * calls to search for DUNS Direct Plus information by duns ID.
 * 
 * You will need to set the D&B Host, Consumer Key and Consumer Secret that have been provided to you by D&B.
 * 
 * If you are required to route your requests through a proxy server, then provide the configuration details to allow
 * the requests to be routed.
 * 
 * This example depends on some open-source Java libraries that must be obtained separately. See accompanying
 * documentation for details.
 * 
 * Note: A service call will fail if your subscription does not include that product.
 */
public class DirectPlusClientRunner {

    public static void main(String[] args) {

        try {
            final String host = "plus.dnb.com";
            final String consumerKey = "<your-client-id>";
            final String consumerSecret = "<your-client-secret>";

            ProxyConfig proxyConfig = null;

            // Optional: If you need to route through a proxy server, then provide proxy details
            // If proxy is not needed, comment out this line
            proxyConfig = new ProxyConfig("host", 3128, "username", "password");

            // Construct the DirectPlusClient instance
            final DirectPlusClient client = new DirectPlusClient(host, proxyConfig);

            // Obtain an access token using your consumer key and consumer secret
            final String accessToken = client.getAccessToken(consumerKey, consumerSecret);

            // Call Direct Plus service to get DUNS data
            final String result = client.getByDunsNumber(accessToken, "590100004", "cmplnk", "v1");

            System.out.println(result);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
