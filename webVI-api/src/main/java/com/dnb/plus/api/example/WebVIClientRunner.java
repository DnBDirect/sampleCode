package com.dnb.plus.api.example;

/**
 * This is an example class to demonstrate a call to the D&B Direct Plus API to obtain an access token and subsequent
 * calls to search for DUNS WebVI information by domain, email and IP address.
 * 
 * You will need to set the D&B Host, Consumer Key and Consumer Secret that have been provided to you by D&B.
 * 
 * If you are required to route your requests through a proxy server, then provide the configuration details to allow
 * the requests to be routed.
 * 
 * This example depends on some open-source Java libraries that must be obtained separately. See accompanying
 * documentation for details.
 * 
 * Note: A service call will fail if your subscription does not include that product
 * 
 */
public class WebVIClientRunner {

    public static void main(String[] args) {
        try {
            final String host = "plus.dnb.com";
            final String consumerKey = "<your-client-id>";
            final String consumerSecret = "<your-client-secret>";

            final String VIEW_STANDARD = "standard";
            final String VIEW_BASIC = "basic";
            final String IP_DOMAIN_TYPE_BIZ = "biz";
            final String IP_DOMAIN_TYPE_ISP = "isp";
            final String IP_DOMAIN_TYPE_ANY = "any";

            ProxyConfig proxyConfig = null;
            // Optional: If you need to route through a proxy server, then provide proxy details
            // Parameters are host, port (both mandatory), username, password (both optional)
            // If proxy is not needed, comment out this line
            proxyConfig = new ProxyConfig("host", 3128, "username", "password");

            // Construct the WebVIClientExample instance
            WebVIClientExample client = new WebVIClientExample(host, proxyConfig);

            // Obtain an accessToken using your consumerKey and consumerSecret
            String accessToken = client.getAccessToken(consumerKey, consumerSecret);

            // Call WebVI service to get standard information by domain
            String result = client.getByDomain("dnb.com", VIEW_STANDARD, accessToken);
            System.out.println(result);

            // Call WebVI service to get basic information by domain
            result = client.getByDomain("dnb.com", VIEW_BASIC, accessToken);
            System.out.println(result);

            // Call WebVI service to get standard information by email address
            result = client.getByEmail("president@whitehouse.gov", VIEW_STANDARD, accessToken);
            System.out.println(result);

            // Call WebVI service to get basic information by email address
            result = client.getByEmail("president@whitehouse.gov", VIEW_BASIC, accessToken);
            System.out.println(result);

            // Call WebVI service to get basic information by IP address
            result = client.getByIP("8.8.8.8", VIEW_STANDARD, accessToken);
            System.out.println(result);

            // Call WebVI service to get standard information by IP address
            result = client.getByIP("8.8.8.8", VIEW_BASIC, accessToken);
            System.out.println(result);

            // Call WebVI service to get standard information by IP resource (type biz)
            result = client.getByIPResource("158.151.240.118", IP_DOMAIN_TYPE_BIZ, VIEW_STANDARD, accessToken);
            System.out.println(result);

            // Call WebVI service to get basic information by IP resource (type biz)
            result = client.getByIPResource("158.151.240.118", IP_DOMAIN_TYPE_BIZ, VIEW_BASIC, accessToken);
            System.out.println(result);

            // Call WebVI service to get standard information by IP resource (type isp)
            result = client.getByIPResource("8.8.8.8", IP_DOMAIN_TYPE_ISP, VIEW_STANDARD, accessToken);
            System.out.println(result);

            // Call WebVI service to get standard information by IP resource (type any)
            result = client.getByIPResource("158.151.240.118", IP_DOMAIN_TYPE_ANY, VIEW_STANDARD, accessToken);
            System.out.println(result);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
