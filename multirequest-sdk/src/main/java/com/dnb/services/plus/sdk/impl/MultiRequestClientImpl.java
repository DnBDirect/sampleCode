/**
 * Copyrighted as an unpublished work 2016 D&B.
 * Proprietary and Confidential.  Use, possession and disclosure subject to license agreement.
 * Unauthorized use, possession or disclosure is a violation of D&B's legal rights and may result
 * in suit or prosecution.
 */
package com.dnb.services.plus.sdk.impl;

import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.SSEAlgorithm;
import com.amazonaws.util.Md5Utils;
import com.dnb.services.plus.sdk.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author SojkaW
 */
public class MultiRequestClientImpl implements MultiRequestClient {

    private static final Logger LOGGER = Logger.getLogger(MultiRequestClientImpl.class);
    private static final String URL_TEMPLATE = "%sprocessID=%s&processVersion=%s&inputFileName=%s&customerReference=%s";
    private static final String DEFAULT_PROPERTY_FILE = "client.properties";
    private static final String TOKEN_REQUEST_JSON = "{ \"grant_type\" : \"client_credentials\" }";
    private static final String PROCESSING_COMPLETED_MSG = "Processed";
    private String requestUrl;
    private String statusUrl;
    private String apigeeTokenUrl;
    private String processId;
    private String processVersion;
    private String customerReference;
    private CloseableHttpClient httpClient;
    private int maxAttempts;
    private int retryInterval;
    int allowableErrorCode;
    boolean errorFilterEnabled;

    public MultiRequestClientImpl() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream(DEFAULT_PROPERTY_FILE));
        } catch (IOException e) {
            throw new RuntimeException("properties file not found", e);
        }
        String proxyHost = properties.getProperty("proxy.host");
        int proxyPort = Integer.valueOf(properties.getProperty("proxy.port"));
        String proxyUser = properties.getProperty("proxy.user");
        String proxyPass = properties.getProperty("proxy.pass");
        String proxyScheme = properties.getProperty("proxy.scheme");
        requestUrl = properties.getProperty("api.request");
        statusUrl = properties.getProperty("api.status");
        processId = properties.getProperty("api.process_id");
        processVersion = properties.getProperty("api.process_version");
        customerReference = properties.getProperty("api.customer_reference");
        apigeeTokenUrl = properties.getProperty("apigee.token.url");
        maxAttempts = (Integer.parseInt(properties.getProperty("api.retry_attempts")));
        retryInterval = (Integer.parseInt(properties.getProperty("api.retry_interval")));
        allowableErrorCode = Integer.parseInt((properties.getProperty("api.allowable_error_code")));
        
        try
        {
        	if (properties.getProperty("api.error_filter").equals("on"))
        		errorFilterEnabled = true;
        	
        	else
        		errorFilterEnabled = false;
        }
        
        catch(Exception e)
        {        	
        	errorFilterEnabled = false;
        }
        

        HttpHost proxy = new HttpHost(proxyHost, proxyPort, proxyScheme);
        Credentials credentials = new UsernamePasswordCredentials(proxyUser, proxyPass);
        AuthScope authScope = new AuthScope(proxyHost, proxyPort);
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(authScope, credentials);
        httpClient = HttpClientBuilder.create().setProxy(proxy).setDefaultCredentialsProvider(credsProvider).build();
    }

    @Override
    public String getAccessToken(String accessKey, String secretKey) throws IOException, AuthenticationException {
        checkStringNotEmpty(accessKey, "accessKey argument is incorrect");
        checkStringNotEmpty(secretKey, "secretKey argument is incorrect");
        LOGGER.info("requesting access token");
        URL urlToCall = new URL(apigeeTokenUrl);
        HttpPost postReq = new HttpPost(URI.create(urlToCall.toExternalForm()));
        postReq.addHeader("Content-Type", "application/json; charset=utf-8");
        postReq.setEntity(new StringEntity(TOKEN_REQUEST_JSON));
        UsernamePasswordCredentials creds =
                new UsernamePasswordCredentials(accessKey, secretKey);
        postReq.addHeader(new BasicScheme().authenticate(creds, postReq, null));
        CloseableHttpResponse httpResponse = null;;
        
        String response = null;
                    
                
        response = runPath(httpResponse, response, postReq);
        
        ObjectMapper mapper = new ObjectMapper();
        TokenResponse token = mapper.readValue(response, TokenResponse.class);
        return token.getAccess_token();
    }

    @Override
    public String generateCustomerKey() {
        LOGGER.info("generating Base64 encoded AES256 customer key");
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(256, new SecureRandom());
            return Base64.encodeBase64String(generator.generateKey().getEncoded());
        } catch (Exception e) {
            LOGGER.trace("problem with generating secret key", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public JobDetails getUploadUrl(String accessToken, String customerKey, String path) throws IOException {
        checkStringNotEmpty(accessToken, "accessToken argument is incorrect");
        checkStringNotEmpty(customerKey, "customerKey argument is incorrect");
        checkStringNotEmpty(path, "path argument is incorrect");

        LOGGER.info("requesting signed URL for file upload");

        checkFileExists(path);
        File file = new File(path);
        String fullUrl = String.format(URL_TEMPLATE, requestUrl, processId, processVersion, file.getName(), customerReference);
        URL urlToCall = new URL(fullUrl);
        HttpGet getreq = new HttpGet(URI.create(urlToCall.toExternalForm()));
        getreq.addHeader("Authorization", "Bearer " + accessToken);
        getreq.addHeader("customer-key", customerKey);
        getreq.addHeader("Content-Type", "application/json; charset=utf-8");
        CloseableHttpResponse httpResponse = null;
        
        String response = null;
                
                
        response = runPath(httpResponse, response, getreq);
                        
        ObjectMapper mapper = new ObjectMapper();
        RequestEndpointResponse reqResp = mapper.readValue(response, RequestEndpointResponse.class);
        
        if (reqResp == null || reqResp.getJobSubmissionDetail() == null || reqResp.getJobSubmissionDetail().getContentURL() == null)
            throw new RuntimeException("error while requesting AWS S3 upload URL");
        JobDetails job = new JobDetails();
        job.setJobId(reqResp.getJobID());
        job.setUploadUrl(reqResp.getJobSubmissionDetail().getContentURL());
        return job;
    }

    public int uploadFileToUrl(String url, String customerKey, String path) throws IOException {
        checkStringNotEmpty(url, "url argument is incorrect");
        checkStringNotEmpty(customerKey, "customerKey argument is incorrect");
        checkStringNotEmpty(path, "path argument is incorrect");
        LOGGER.info("uploading file to AWS S3 storage");

        checkFileExists(path);
        URL urlToCall = new URL(url);
        byte[] decodedKey = Base64.decodeBase64(customerKey);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        HttpPut putreq = new HttpPut(URI.create(urlToCall.toExternalForm()));
        putreq.addHeader(
                new BasicHeader(Headers.SERVER_SIDE_ENCRYPTION_CUSTOMER_ALGORITHM, SSEAlgorithm.AES256.getAlgorithm()));
        putreq.addHeader(new BasicHeader(Headers.SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY, customerKey));
        putreq.addHeader(new BasicHeader(Headers.SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY_MD5,
                Md5Utils.md5AsBase64(originalKey.getEncoded())));
        putreq.setEntity(new FileEntity(new File(path)));
        CloseableHttpResponse  httpResponse = httpClient.execute(putreq);
        int status = httpResponse.getStatusLine().getStatusCode();
        httpResponse.close();
        return status;
    }


    @Override
    public UploadResponse uploadFile(String accessToken, String customerKey, String path) throws IOException {
        checkStringNotEmpty(accessToken, "accessToken argument is incorrect");
        checkStringNotEmpty(customerKey, "customerKey argument is incorrect");
        checkStringNotEmpty(path, "path argument is incorrect");

        checkFileExists(path);
        JobDetails job = getUploadUrl(accessToken, customerKey, path);
        int status = uploadFileToUrl(job.getUploadUrl(), customerKey, path);
        UploadResponse resp = new UploadResponse();
        resp.setSubmissionId(job.getJobId());
        resp.setStatusCode(status);
        return resp;
    }

    @Override
    public List<UploadResponse> uploadFiles(String accessToken, String customerKey, List<String> paths) throws IOException {
        checkStringNotEmpty(accessToken, "accessToken argument is incorrect");
        checkStringNotEmpty(customerKey, "customerKey argument is incorrect");

        List<UploadResponse> responses = new ArrayList<UploadResponse>(paths.size());
        for (String path : paths) {
            responses.add(uploadFile(accessToken, customerKey, path));
        }
        return responses;
    }

    @Override
    public StatusResponse getStatus(String submissionId, String customerKey, String accessToken) throws IOException {
        checkStringNotEmpty(submissionId, "submissionId argument is incorrect");
        checkStringNotEmpty(customerKey, "customerKey argument is incorrect");
        checkStringNotEmpty(accessToken, "accessToken argument is incorrect");

        LOGGER.info("requesting status for job " + submissionId);
        URL urlToCall = new URL(statusUrl + submissionId);
        HttpGet getreq = new HttpGet(URI.create(urlToCall.toExternalForm()));
        getreq.addHeader("Authorization", "Bearer " + accessToken);
        getreq.addHeader("Content-Type", "application/json; charset=utf-8");
        getreq.addHeader("Customer-Key", customerKey);
        CloseableHttpResponse httpResponse = null;    
              
        String response = null;
        
        response = runPath(httpResponse, response, getreq);
        
        
        ObjectMapper mapper = new ObjectMapper();
        StatusEndpointResponse statResp = mapper.readValue(response, StatusEndpointResponse.class);

        StatusResponse resp = new StatusResponse();
        if (statResp.getInformation() != null) {
            resp.setStatusCode(statResp.getInformation().getMessage());
            if (PROCESSING_COMPLETED_MSG.equals(statResp.getInformation().getMessage()))
                resp.setDownloadUrl(statResp.getOutputDetail().getContentURL());
        }
        return resp;
    }

    @Override
    public List<StatusResponse> getStatus(List<String> submissionIds, String customerKey, String accessToken) throws IOException {
        checkStringNotEmpty(customerKey, "customerKey argument is incorrect");
        checkStringNotEmpty(accessToken, "accessToken argument is incorrect");

        List<StatusResponse> responses = new ArrayList<StatusResponse>(submissionIds.size());
        for (String submissionId : submissionIds) {
            responses.add(getStatus(submissionId, customerKey, accessToken));
        }
        return responses;
    }

    @Override
    public DownloadResponse downloadFile(String downloadUrl, String submissionId, String folderPath, String customerKey) throws IOException {
        checkStringNotEmpty(downloadUrl, "download url argument is incorrect");
        checkStringNotEmpty(submissionId, "submissionID argument is incorrect");
        checkStringNotEmpty(folderPath, "folderPath argument is incorrect");
        checkStringNotEmpty(customerKey, "customerKey argument is incorrect");

        LOGGER.info("downloading results file from AWS");
        URL urlToCall = new URL(downloadUrl);
        String filename = extractFileName(urlToCall);

        File folder = new File(folderPath);
        if (!folder.isDirectory() || !folder.canWrite())
            throw new RuntimeException("destination directory doesn't exist or has wrong permission");

        File file = new File(folder.getAbsolutePath() + File.separator + filename);
        HttpGet getreq = new HttpGet(URI.create(urlToCall.toExternalForm()));
        byte[] decodedKey = Base64.decodeBase64(customerKey);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        getreq.addHeader(
                new BasicHeader(Headers.SERVER_SIDE_ENCRYPTION_CUSTOMER_ALGORITHM, SSEAlgorithm.AES256.getAlgorithm()));
        getreq.addHeader(new BasicHeader(Headers.SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY, customerKey));
        getreq.addHeader(new BasicHeader(Headers.SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY_MD5,
                Md5Utils.md5AsBase64(originalKey.getEncoded())));
        CloseableHttpResponse httpResponse = httpClient.execute(getreq);

        int status = httpResponse.getStatusLine().getStatusCode();
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        try {
            httpResponse.getEntity().writeTo(bos);
        } catch (IOException e) {
            throw new RuntimeException("error while saving response to a file", e);
        } finally {
            try {
                bos.close();
                httpResponse.close();
            } catch (IOException e) {
                LOGGER.error("error while closing streams", e);
            }

        }
        DownloadResponse resp = new DownloadResponse();
        resp.setSubmissionId(submissionId);
        resp.setStatus(status);
        resp.setDownloadPath(file.getAbsolutePath());
        return resp;
    }

    @Override
    public List<DownloadResponse> downloadFiles(Map<String, String> toDownload, String downloadPath, String customerKey) throws IOException {
        checkStringNotEmpty(downloadPath, "downloadPath argument is incorrect");
        checkStringNotEmpty(customerKey, "customerKey argument is incorrect");

        List<DownloadResponse> responses = new ArrayList<DownloadResponse>(toDownload.size());
        for (Map.Entry<String, String> entry : toDownload.entrySet()) {
            responses.add(downloadFile(entry.getKey(), entry.getValue(), downloadPath, customerKey));
        }
        return responses;
    }

    @Override
    public OutputStream downloadFile(String downloadUrl, String submissionId, String customerKey) throws IOException {
        checkStringNotEmpty(downloadUrl, "download url argument is incorrect");
        checkStringNotEmpty(submissionId, "submissionID argument is incorrect");
        checkStringNotEmpty(customerKey, "customerKey argument is incorrect");

        LOGGER.info("downloading results file from AWS");
        URL urlToCall = new URL(downloadUrl);
       
        HttpGet getreq = new HttpGet(URI.create(urlToCall.toExternalForm()));
        byte[] decodedKey = Base64.decodeBase64(customerKey);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        getreq.addHeader(
                new BasicHeader(Headers.SERVER_SIDE_ENCRYPTION_CUSTOMER_ALGORITHM, SSEAlgorithm.AES256.getAlgorithm()));
        getreq.addHeader(new BasicHeader(Headers.SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY, customerKey));
        getreq.addHeader(new BasicHeader(Headers.SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY_MD5,
                Md5Utils.md5AsBase64(originalKey.getEncoded())));
        CloseableHttpResponse httpResponse = httpClient.execute(getreq);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            httpResponse.getEntity().writeTo(os);
        } catch (IOException e) {
            throw new RuntimeException("error while saving response to outputstream", e);
        } finally {
            try {
                httpResponse.close();
            } catch (IOException e) {
                LOGGER.error("error while closing streams", e);
            }

        }
        return os;
    }

    public List<OutputStream> downloadFiles(Map<String, String> toDownload, String customerKey) throws IOException {
    	checkStringNotEmpty(customerKey, "customerKey argument is incorrect");

        List<OutputStream> responses = new ArrayList<OutputStream>(toDownload.size());
        for (Map.Entry<String, String> entry : toDownload.entrySet()) {
            responses.add(downloadFile(entry.getKey(), entry.getValue(), customerKey));
        }
        return responses;
    }

    private void checkFileExists(String path) {
        File file = new File(path);
        if (!file.exists() || !file.canRead())
            throw new RuntimeException("unable to read source file: " + file);
    }

    private void checkStringNotEmpty(String in, String message) {
        if (in == null || in.isEmpty())
            throw new IllegalArgumentException(message);
    }

    private String extractFileName(URL url) {
        File f = new File(url.getPath());
        return f.getName();
    }
    
    private String runPath(CloseableHttpResponse httpResponse, String response, HttpUriRequest getReq) throws ClientProtocolException, IOException
    {
    	int currentAttempt = 1;
    	if (errorFilterEnabled == true)
            
        {
        	while (currentAttempt <= maxAttempts)
        
        	{
        		try
        		{
        			LOGGER.info("Attempt number " + currentAttempt + " of " + maxAttempts + " to connect...");
        			httpResponse = httpClient.execute(getReq);
        			response = new BasicResponseHandler().handleResponse(httpResponse);
        			break;
        		}
        
        		catch(Exception e)
        		{
        			
        			int responseCode = httpResponse.getStatusLine().getStatusCode();
        			try 
        			{
        				if(currentAttempt == maxAttempts)
        				{
        					LOGGER.error("Maximum number of retry attempts reached. Exiting...");
        					System.exit(-101);
        				}
        			
        				else if (responseCode != allowableErrorCode)
        				{	
        					LOGGER.error("Quitting due to the following unexpected response code: " + httpResponse.getStatusLine().getStatusCode() + " and response message: " + httpResponse.getStatusLine().getReasonPhrase());
        					System.exit(-102);
        				}
					
        				else 
        				{	
        					LOGGER.error("Getting the response : \"" + httpResponse.getStatusLine().getStatusCode() + " " + httpResponse.getStatusLine().getReasonPhrase() + "\" . Re-attempting in " + retryInterval + " seconds...");
        					Thread.sleep(retryInterval * 1000);
        				}
        			}
        		
        			catch (Exception e1)
        			{        			
        			}
        		
        			currentAttempt += 1;
        		}
        	}
        }
        
        else
        {
        	httpResponse = httpClient.execute(getReq);
        	response = new BasicResponseHandler().handleResponse(httpResponse);
        }
    	httpResponse.close();
		return response;
    }        
}