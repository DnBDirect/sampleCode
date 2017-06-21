package com.dnb.plus.api.example;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * An example class that demonstrates calls to the D&B WebVI API
 */
public class WebVIClientExample {

    private static final String WEBVI_PATH_AUTH = "/v2/token";
    private static final String WEBVI_PATH_DUNS_SEARCH = "/v1/duns-search";
    private static final String WEBVI_PATH_GET_BY_DOMAIN = WEBVI_PATH_DUNS_SEARCH + "/domain/";
    private static final String WEBVI_PATH_GET_BY_EMAIL = WEBVI_PATH_DUNS_SEARCH + "/email/";
    private static final String WEBVI_PATH_GET_BY_IP = WEBVI_PATH_DUNS_SEARCH + "/ip/";
    private static final String WEBVI_PATH_GET_BY_IP_RESOURCE = WEBVI_PATH_DUNS_SEARCH + "/ipresource/";

    private String plusHost;
    private ProxyConfig proxyConfig;

    public WebVIClientExample(String plusHost) {
        this.plusHost = plusHost;
    }

    public WebVIClientExample(String plusHost, ProxyConfig proxyConfig) {
        this(plusHost);
        this.proxyConfig = proxyConfig;
    }

    /*
     * An example of a call to the D&B Direct Plus API to obtain an access token from using a Consumer Key and Consumer
     * Secret
     * 
     * You would normally be expected to cache this on your client and re-use it for subsequent requests.
     * 
     * Purely an example, this method is not at all robust and does not handle any of the exception conditions that can
     * be normally expected.
     */
    public String getAccessToken(String consumerKey, String consumerSecret) throws IOException,
            ClientProtocolException, JSONException, URISyntaxException {
        String cred = consumerKey + ":" + consumerSecret;
        String auth = new String(Base64.encodeBase64(cred.getBytes()));

        URIBuilder builder = new URIBuilder();
        builder.setScheme("https");
        builder.setHost(plusHost);
        builder.setPath(WEBVI_PATH_AUTH);
        URI uri = builder.build();

        HttpPost request = new HttpPost(uri);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Authorization", "Basic " + auth);
        request.setHeader("Origin", "www.dnb.com");
        StringEntity requestEntity = new StringEntity("{ \"grant_type\" : \"client_credentials\" }");
        request.setEntity(requestEntity);

        HttpResponse response = execute(request);
        String content = EntityUtils.toString(response.getEntity());
        return new JSONObject(content).getString("access_token");
    }

    /*
     * An example of a call to the D&B WebVI API to search for DUNS information by Domain
     */
    public String getByDomain(String domain, String view, String accessToken) throws IOException,
            ClientProtocolException, URISyntaxException {
        return callService(WEBVI_PATH_GET_BY_DOMAIN, domain, view, accessToken);
    }

    /*
     * An example of a call to the D&B WebVI API to search for DUNS information by Email Address
     */
    public String getByEmail(String emailAddress, String view, String accessToken) throws IOException,
            ClientProtocolException, URISyntaxException {
        return callService(WEBVI_PATH_GET_BY_EMAIL, emailAddress, view, accessToken);
    }

    /*
     * An example of a call to the D&B WebVI API to search for DUNS information by IP Address
     */
    public String getByIP(String ipAddress, String view, String accessToken) throws IOException,
            ClientProtocolException, URISyntaxException {
        return callService(WEBVI_PATH_GET_BY_IP, ipAddress, view, accessToken);
    }

    /*
     * An example of a call to the D&B WebVI API to search for DUNS information by IP Resource
     */
    public String getByIPResource(String ipAddress, String ipDomainType, String view, String accessToken)
            throws IOException, ClientProtocolException, URISyntaxException {
        return callService(WEBVI_PATH_GET_BY_IP_RESOURCE, ipAddress, view, accessToken, new BasicNameValuePair(
                "ipDomainType", ipDomainType));
    }

    /*
     * An example of a call to the D&B WebVI API
     * 
     * Purely an example, this method is not at all robust and does not handle any of the exception conditions that can
     * be normally expected, such as expiration of the accessToken (requiring re-authentication before any more services
     * can be invoked)
     */
    public String callService(String serviceName, String param, String view, String accessToken,
            NameValuePair... queryParams) throws IOException, ClientProtocolException, URISyntaxException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("https").setHost(plusHost).setPath(serviceName + param);
        addQueryParam(builder, "view", view);
        for (NameValuePair queryParam : queryParams) {
            addQueryParam(builder, queryParam.getName(), queryParam.getValue());
        }
        URI uri = builder.build();
        HttpGet request = new HttpGet(uri);

        request.setHeader("Content-Type", "application/json");
        request.setHeader("Authorization", "Bearer " + accessToken);
        request.setHeader("Origin", "www.dnb.com");

        HttpResponse response = execute(request);
        return EntityUtils.toString(response.getEntity());
    }

    /*
     * Adds a query parameter to the builder, if value is not null
     */
    private void addQueryParam(URIBuilder builder, String name, String value) {
        if (value != null) {
            builder.addParameter(name, value);
        }
    }

    /*
     * Executes a HTTP request, routing through a proxy server if so configured
     */
    private HttpResponse execute(HttpRequestBase request) throws IOException, ClientProtocolException {
        HttpClientBuilder builder = HttpClientBuilder.create();
        if (proxyConfig != null) {
            setProxyServer(request);
            buildProxyCredentials(builder);
        }
        CloseableHttpClient httpclient = builder.build();
        System.out.println("Calling URI: " + request.getURI());
        return httpclient.execute(request);
    }

    private void buildProxyCredentials(HttpClientBuilder builder) {
        if (proxyConfig.getUsername() != null && proxyConfig.getPassword() != null) {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(proxyConfig.getHost(), proxyConfig.getPort()),
                    new UsernamePasswordCredentials(proxyConfig.getUsername(), proxyConfig.getPassword()));
            builder.setDefaultCredentialsProvider(credsProvider);
        }
    }

    private void setProxyServer(HttpRequestBase request) {
        HttpHost proxy = new HttpHost(proxyConfig.getHost(), proxyConfig.getPort(), "http");
        RequestConfig reqConfig = RequestConfig.custom().setProxy(proxy).setAuthenticationEnabled(true)
                .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).build();
        request.setConfig(reqConfig);
    }
}
