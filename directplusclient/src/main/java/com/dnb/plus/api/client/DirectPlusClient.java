package com.dnb.plus.api.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
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
 * A client class that demonstrates calls to the D&B Direct Plus API.
 */
public class DirectPlusClient {

    private static final String MDM_PATH_AUTH = "/v2/token";
    private static final String MDM_PATH_DUNS_SEARCH = "/v1/data";
    private static final String MDM_PATH_GET_BY_DUNS = MDM_PATH_DUNS_SEARCH + "/duns/";

    private String directPlusHost;

    private ProxyConfig proxyConfig;

    public DirectPlusClient(String plusHost) {
        this(plusHost, null);
    }

    public DirectPlusClient(String plusHost, ProxyConfig proxyConfig) {
        this.directPlusHost = plusHost;
        this.proxyConfig = proxyConfig;
    }

    /**
     * Obtains an access token from using a consumer key and consumer secret in order to make a call to the D&B Direct
     * Plus API.
     * 
     * You would normally be expected to cache this on your client and re-use it for subsequent requests.
     * 
     * Purely an example, this method is not at all robust and does not handle any of the exceptional conditions that
     * can be normally expected.
     */
    public String getAccessToken(String consumerKey, String consumerSecret)
            throws IOException, JSONException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException {

        final String cred = consumerKey + ":" + consumerSecret;
        final String auth = new String(Base64.encodeBase64(cred.getBytes()));

        final URI uri = new URIBuilder().setScheme("https").setHost(directPlusHost).setPath(MDM_PATH_AUTH).build();

        final HttpPost request = new HttpPost(uri);
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + auth);
        request.setHeader("Origin", "www.dnb.com");

        final StringEntity requestEntity = new StringEntity("{ \"grant_type\" : \"client_credentials\" }");
        request.setEntity(requestEntity);

        final HttpResponse response = execute(request);
        final String content = EntityUtils.toString(response.getEntity());

        System.out.println("Content received: " + content);

        return new JSONObject(content).getString("access_token");
    }

    /**
     * Makes a call to the D&B Direct Plus API to search for DUNS information by DUNS number.
     */
    public String getByDunsNumber(String accessToken, String dunsNumber, String productId, String versionId)
            throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException {

        return callService(MDM_PATH_GET_BY_DUNS, accessToken, dunsNumber,
                new BasicNameValuePair("productId", productId), new BasicNameValuePair("versionId", versionId));
    }

    /**
     * Makes a call to the D&B Direct Plus API by using the service name, service parameter, query parameters, and the
     * access token provided.
     * 
     * Purely an example, this method is not at all robust and does not handle any of the exceptional conditions that
     * can be normally expected, such as expiration of the accessToken (requiring re-authentication before any more
     * services can be invoked).
     */
    public String callService(String serviceName, String accessToken, String param, NameValuePair... queryParams)
            throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException {

        final URIBuilder builder = new URIBuilder().setScheme("https").setHost(directPlusHost)
                .setPath(serviceName + param);

        for (final NameValuePair queryParam : queryParams) {
            addQueryParam(builder, queryParam.getName(), queryParam.getValue());
        }

        final URI uri = builder.build();

        final HttpGet request = new HttpGet(uri);
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        request.setHeader("Origin", "www.dnb.com");

        final HttpResponse response = execute(request);

        final String content = EntityUtils.toString(response.getEntity());

        System.out.println("Content received: " + content);

        return content;
    }

    /**
     * Adds a query parameter to the builder unless the value is null.
     */
    private void addQueryParam(URIBuilder builder, String name, String value) {
        if (value != null) {
            builder.addParameter(name, value);
        }
    }

    /**
     * Executes an HTTP request. (routing through a proxy server if configured)
     */
    private HttpResponse execute(HttpRequestBase request)
            throws IOException, NoSuchAlgorithmException, KeyManagementException {

        final HttpClientBuilder builder = HttpClientBuilder.create();

        if (proxyConfig != null) {
            setProxyServer(request);
            buildProxyCredentials(builder);
        }

        // Comment out the following block if you are testing against an invalid SSL certificate (non PROD).
        final CloseableHttpClient httpclient = builder.build();

        // Uncomment out the following block if you are testing against an invalid SSL certificate (non PROD).
        // SSLContext sslContext = SSLContext.getInstance("TLS");
        // sslContext.init(null, null, null);
        // SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
        // SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        // CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        System.out.println("Calling URI: " + request.getURI());

        return httpclient.execute(request);
    }

    private void buildProxyCredentials(HttpClientBuilder builder) {
        if (proxyConfig.getUsername() != null && proxyConfig.getPassword() != null) {
            final AuthScope authScope = new AuthScope(proxyConfig.getHost(), proxyConfig.getPort());
            final UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(
                    proxyConfig.getUsername(), proxyConfig.getPassword());

            final CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(authScope, usernamePasswordCredentials);

            builder.setDefaultCredentialsProvider(credsProvider);
        }
    }

    private void setProxyServer(HttpRequestBase request) {
        final HttpHost proxy = new HttpHost(proxyConfig.getHost(), proxyConfig.getPort(), "http");
        final RequestConfig reqConfig = RequestConfig.custom().setProxy(proxy).setAuthenticationEnabled(true)
                .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).build();
        request.setConfig(reqConfig);
    }
}
