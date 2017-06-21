package com.dnb.daas.match;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;

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
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * An example class that demonstrates calls to the D&B MatchPlus API
 */
public class MatchClientExample {

    private static final String API_PATH_AUTH = "/v2/token";
    private static final String API_PATH_CLEANSE_MATCH = "/v1/match/cleanseMatch";

    private String host;
    private ProxyConfig proxyConfig;

    public MatchClientExample(String host) {
        this.host = host;
    }

    public MatchClientExample(String host, ProxyConfig proxyConfig) {
        this(host);
        this.proxyConfig = proxyConfig;
    }

    /*
     * An example of a call to the D&B DirectPlus API to obtain an access token from using a Consumer Key and Consumer Secret
     * 
     * You would normally be expected to cache this on your client and re-use it for subsequent requests.
     * 
     * Purely an example, this method is not at all robust and does not handle any of the exception conditions that can
     * be normally expected.
     */
    public String getAccessToken(String consumerKey, String consumerSecret) throws IOException,
            ClientProtocolException, JSONException, KeyManagementException, UnrecoverableKeyException,
            NoSuchAlgorithmException, KeyStoreException, CertificateException {
        String cred = consumerKey + ":" + consumerSecret;
        String auth = new String(Base64.encodeBase64(cred.getBytes()));
        String url = "https://" + host + API_PATH_AUTH;

        HttpPost request = new HttpPost(url);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Authorization", "Basic " + auth);
        request.setHeader("Origin", "www.dnb.com");
        StringEntity requestEntity = new StringEntity("{ \"grant_type\" : \"client_credentials\" }");
        request.setEntity(requestEntity);

        HttpResponse response = execute(request);
        String content = EntityUtils.toString(response.getEntity());
        System.out.println("===> Token response [" + content + "]");
        return new JSONObject(content).getString("access_token");
    }

    /*
     * An example of a call to the D&B API
     * 
     * Purely an example, this method is not at all robust and does not handle any of the exception conditions that can
     * be normally expected, such as expiration of the accessToken (requiring re-authentication before any more services
     * can be invoked)
     */
    public String cleanseMatch(List<NameValuePair> callParameters, String accessToken) throws IOException,
            ClientProtocolException, URISyntaxException, KeyManagementException, UnrecoverableKeyException,
            NoSuchAlgorithmException, KeyStoreException, CertificateException {
        URIBuilder builder = new URIBuilder();
        builder.setHost(host);
        builder.setPath(API_PATH_CLEANSE_MATCH);
        builder.setScheme("https");
        builder.setParameters(callParameters);
        URI uri = builder.build();
        System.out.println("==URI==>" + uri);
        System.out.println("==URI (ascii)==>" + uri.toASCIIString());

        HttpGet request = new HttpGet(uri);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Authorization", "Bearer " + accessToken);
        request.setHeader("Origin", "www.dnb.com");
        System.out.println("==URI==> Authorization Bearer " + accessToken);
        request.setHeader("Origin", "www.dnb.com");

        HttpResponse response = execute(request);
        return EntityUtils.toString(response.getEntity());
    }

    /*
     * Executes a HTTP request, routing through a proxy server if so configured
     */
    private HttpResponse execute(HttpRequestBase request) throws IOException, ClientProtocolException,
            KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException,
            CertificateException {
        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setSSLHostnameVerifier(new NoopHostnameVerifier());
        if (proxyConfig != null) {
            setProxyServer(request);
            buildProxyCredentials(builder);
        }
        CloseableHttpClient httpclient = builder.build();
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
