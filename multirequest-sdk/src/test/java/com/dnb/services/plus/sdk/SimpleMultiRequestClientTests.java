package com.dnb.services.plus.sdk;

import com.dnb.services.plus.sdk.impl.MultiRequestClientImpl;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author SojkaW
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleMultiRequestClientTests {

    @Mock
    private CloseableHttpClient httpClient = mock(CloseableHttpClient.class);

    @InjectMocks
    private MultiRequestClientImpl client = new MultiRequestClientImpl();

    private BasicHttpEntity entityMock = mock(BasicHttpEntity.class);
    private StatusLine statusLineMock = mock(StatusLine.class);
    private CloseableHttpResponse response;
    private static final String TOKEN_RESPONSE = "{ \"access_token\": \"AAABBBCCC\", \"expiresIn\": 86400 }";
    private static final String REQUEST_RESPONSE = "{ \"transactionDetail\": { \"_comment\":\"HTTP Status 202 Accepted\", \"transactionID\": \"TRN456\", \"transactionTimestamp\": \"2016-04-28T19:43:37Z\" }, \"information\": { \"code\": \"39000\", \"message\": \"Multi process job accepted. Please check status and proceed\", \"url\":\"https://plus.dnb.com/v1/multiProcess/jobStatus?jobID=TEST-CUST123-multi1\", \"method\":\"GET\" }, \"_comment\": \"Only in case of an error. Mutually exclusive with information section\", \"error\": { \"errorCode\": \"10020\", \"errorMessage\": \"Invalid format - contentObject\", \"errorInformationURL\": \"http://plus.dnb.com/docs/errors/10020\" },\t  \"_comment\": \"From the customer\", \"requestDetail\": { \"processID\": \"extmatch\", \"processVersion\": \"v1\", \"_comment\": \"The data exchange mode is yet to be finalized between POST attachment, STP, S3\", \"checkSum\": \"d41d8cd98f00b204e9800998ecf8427e\", \"outputFileRetentionPeriod\" : \"P3D\", \"customerReference\": \"Test1\" }, \"jobID\": \"TEST-CUST123-multi1\", \"jobSubmissionDetail\" : { \"contentURL\":\"https://dev-us-east-1.s3.amazonaws.com/multi-request/akhiltestnew/123456789/input.csv?AWSAccessKeyId=AKIAI2W6HC45SXENDWXQ&Expires=1462394734&Signature=J1xkAm4O9PXYxe%2Ff9FFO%2B%2BKFMBI%3D\", \"urlMethod\": \"PUT\", \"urlExpirationTimestamp\": \"2016-05-06T13:14:28Z\" } }";
    private static final String REQUEST_RESPONSE_NULL1 = "{ \"transactionDetail\": { \"_comment\":\"HTTP Status 202 Accepted\", \"transactionID\": \"TRN456\", \"transactionTimestamp\": \"2016-04-28T19:43:37Z\" }, \"information\": { \"code\": \"39000\", \"message\": \"Multi process job accepted. Please check status and proceed\", \"url\":\"https://plus.dnb.com/v1/multiProcess/jobStatus?jobID=TEST-CUST123-multi1\", \"method\":\"GET\" }, \"_comment\": \"Only in case of an error. Mutually exclusive with information section\", \"error\": { \"errorCode\": \"10020\", \"errorMessage\": \"Invalid format - contentObject\", \"errorInformationURL\": \"http://plus.dnb.com/docs/errors/10020\" },\t  \"_comment\": \"From the customer\", \"requestDetail\": { \"processID\": \"extmatch\", \"processVersion\": \"v1\", \"_comment\": \"The data exchange mode is yet to be finalized between POST attachment, STP, S3\", \"checkSum\": \"d41d8cd98f00b204e9800998ecf8427e\", \"outputFileRetentionPeriod\" : \"P3D\", \"customerReference\": \"Test1\" }, \"jobID\": \"TEST-CUST123-multi1\", \"jobSubmissionDetail\" : null }";
    private static final String REQUEST_RESPONSE_NULL2 = "{ \"transactionDetail\": { \"_comment\":\"HTTP Status 202 Accepted\", \"transactionID\": \"TRN456\", \"transactionTimestamp\": \"2016-04-28T19:43:37Z\" }, \"information\": { \"code\": \"39000\", \"message\": \"Multi process job accepted. Please check status and proceed\", \"url\":\"https://plus.dnb.com/v1/multiProcess/jobStatus?jobID=TEST-CUST123-multi1\", \"method\":\"GET\" }, \"_comment\": \"Only in case of an error. Mutually exclusive with information section\", \"error\": { \"errorCode\": \"10020\", \"errorMessage\": \"Invalid format - contentObject\", \"errorInformationURL\": \"http://plus.dnb.com/docs/errors/10020\" },\t  \"_comment\": \"From the customer\", \"requestDetail\": { \"processID\": \"extmatch\", \"processVersion\": \"v1\", \"_comment\": \"The data exchange mode is yet to be finalized between POST attachment, STP, S3\", \"checkSum\": \"d41d8cd98f00b204e9800998ecf8427e\", \"outputFileRetentionPeriod\" : \"P3D\", \"customerReference\": \"Test1\" }, \"jobID\": \"TEST-CUST123-multi1\", \"jobSubmissionDetail\" : { \"contentURL\": null, \"urlMethod\": \"PUT\", \"urlExpirationTimestamp\": \"2016-05-06T13:14:28Z\" } }";
    private static final String STATUS_RESPONSE_PROCESSED = "{ \"transactionDetail\": { \"transactionID\": \"TRN789\", \"transactionTimestamp\": \"2016-04-28T20:20:37Z\" }, \"information\": { \"code\": \"39001\", \"message\": \"Processed\" }, \"_comment\": \"Only in case of an error. Mutually exclusive with information section\", \"error\": { \"errorCode\": \"10031\", \"errorMessage\": \"Status not available\", \"errorInformationURL\": \"http://plus.dnb.com/docs/errors/10020\" }, \"requestDetail\": { \"jobID\": \"TEST-CUST123-multi1\" }, \"jobID\": \"TEST-CUST123-multi1\", \"outputDetail\":{ \"contentURL\":\"https://localhost/file/download/url\", \"urlMethod\": \"GET\", \"urlExpirationTimestamp\": \"2016-05-07T13:14:28Z\", \"totalRecordsCount\": 175000, \"succeededRecordsCount\": 150000 }}";
    private static final String STATUS_RESPONSE_PROCESSING = "{ \"transactionDetail\": { \"transactionID\": \"TRN789\", \"transactionTimestamp\": \"2016-04-28T20:20:37Z\" }, \"information\": { \"code\": \"39001\", \"message\": \"Processing\" }, \"_comment\": \"Only in case of an error. Mutually exclusive with information section\", \"error\": { \"errorCode\": \"10031\", \"errorMessage\": \"Status not available\", \"errorInformationURL\": \"http://plus.dnb.com/docs/errors/10020\" }, \"requestDetail\": { \"jobID\": \"TEST-CUST123-multi1\" }, \"jobID\": \"TEST-CUST123-multi1\", \"outputDetail\":{ \"contentURL\":\"https://localhost/file/download/url\", \"urlMethod\": \"GET\", \"urlExpirationTimestamp\": \"2016-05-07T13:14:28Z\", \"totalRecordsCount\": 175000, \"succeededRecordsCount\": 150000 }}";
    private static final String STATUS_RESPONSE_NULL = "{ \"transactionDetail\": { \"transactionID\": \"TRN789\", \"transactionTimestamp\": \"2016-04-28T20:20:37Z\" }, \"information\": null, \"_comment\": \"Only in case of an error. Mutually exclusive with information section\", \"error\": { \"errorCode\": \"10031\", \"errorMessage\": \"Status not available\", \"errorInformationURL\": \"http://plus.dnb.com/docs/errors/10020\" }, \"requestDetail\": { \"jobID\": \"TEST-CUST123-multi1\" }, \"jobID\": \"TEST-CUST123-multi1\", \"outputDetail\":{ \"contentURL\":\"https://localhost/file/download/url\", \"urlMethod\": \"GET\", \"urlExpirationTimestamp\": \"2016-05-07T13:14:28Z\", \"totalRecordsCount\": 175000, \"succeededRecordsCount\": 150000 }}";
    private static final String CHARSET = "UTF-8";

    @Before
    public void setUp() throws IOException {
        when(statusLineMock.getStatusCode()).thenReturn(200);
        when(statusLineMock.getReasonPhrase()).thenReturn("Success");

        response = mock(CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(statusLineMock);
        when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(response);
    }

    @Test
    public void testGetAccessToken() throws Exception {
        when(entityMock.getContent()).thenReturn(new ByteArrayInputStream(TOKEN_RESPONSE.getBytes(Charset.forName(CHARSET))));
        when(entityMock.getContentLength()).thenReturn(70L);
        when(response.getEntity()).thenReturn(entityMock);

        String token = client.getAccessToken("test", "test");
        assertEquals("AAABBBCCC", token);
    }

    @Test
    public void testUploadFile() throws IOException {
        when(entityMock.getContent()).thenReturn(new ByteArrayInputStream(REQUEST_RESPONSE.getBytes(Charset.forName(CHARSET))));
        when(entityMock.getContentLength()).thenReturn(1242L);
        when(response.getEntity()).thenReturn(entityMock);
        when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        UploadResponse response = client.uploadFile("token", "key", "sample.csv");
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals("TEST-CUST123-multi1", response.getSubmissionId());
    }

    @Test(expected = RuntimeException.class)
    public void testUploadFileJobSubmissionDetailNull() throws IOException {
        when(entityMock.getContent()).thenReturn(new ByteArrayInputStream(REQUEST_RESPONSE_NULL1.getBytes(Charset.forName(CHARSET))));
        when(entityMock.getContentLength()).thenReturn(1242L);
        when(response.getEntity()).thenReturn(entityMock);
        when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        client.uploadFile("token", "key", "sample.csv");
   }

    @Test(expected = RuntimeException.class)
    public void testUploadFileResponseNull() throws IOException {
        when(entityMock.getContent()).thenReturn(new ByteArrayInputStream("null".getBytes(Charset.forName(CHARSET))));
        when(entityMock.getContentLength()).thenReturn(1242L);
        when(response.getEntity()).thenReturn(entityMock);
        when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        client.uploadFile("token", "key", "sample.csv");
    }

    @Test(expected = RuntimeException.class)
    public void testUploadFileContentURLNull() throws IOException {
        when(entityMock.getContent()).thenReturn(new ByteArrayInputStream(REQUEST_RESPONSE_NULL2.getBytes(Charset.forName(CHARSET))));
        when(entityMock.getContentLength()).thenReturn(1242L);
        when(response.getEntity()).thenReturn(entityMock);
        when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        client.uploadFile("token", "key", "sample.csv");
    }

    @Test
    public void testUploadFiles() throws IOException {
        when(entityMock.getContent())
                .thenReturn(new ByteArrayInputStream(REQUEST_RESPONSE.getBytes(Charset.forName(CHARSET))))
                .thenReturn(new ByteArrayInputStream(REQUEST_RESPONSE.getBytes(Charset.forName(CHARSET))))
                .thenReturn(new ByteArrayInputStream(REQUEST_RESPONSE.getBytes(Charset.forName(CHARSET))));
        when(entityMock.getContentLength()).thenReturn(1240L);
        when(response.getEntity()).thenReturn(entityMock);

        List<UploadResponse> responses = client.uploadFiles("token", "key", Arrays.asList(
                new String[] { "sample.csv", "sample.csv", "sample.csv" } ));
        assertNotNull(responses);
        assertEquals(3, responses.size());
        assertEquals(200, responses.get(0).getStatusCode());
        assertEquals("TEST-CUST123-multi1", responses.get(0).getSubmissionId());
        assertEquals(200, responses.get(1).getStatusCode());
        assertEquals("TEST-CUST123-multi1", responses.get(1).getSubmissionId());
        assertEquals(200, responses.get(2).getStatusCode());
        assertEquals("TEST-CUST123-multi1", responses.get(2).getSubmissionId());
    }

    @Test
    public void testGetStatus() throws IOException {
        when(entityMock.getContent()).thenReturn(new ByteArrayInputStream(STATUS_RESPONSE_PROCESSED.getBytes(Charset.forName(CHARSET))));
        when(entityMock.getContentLength()).thenReturn(687L);
        when(response.getEntity()).thenReturn(entityMock);

        StatusResponse response = client.getStatus("submission-id", "customerkey", "accesstoken");
        assertNotNull(response);
        assertEquals("Processed", response.getStatusCode());
        assertEquals("https://localhost/file/download/url", response.getDownloadUrl());
    }

    @Test
    public void testGetStatusProcessing() throws IOException {
        when(entityMock.getContent()).thenReturn(new ByteArrayInputStream(STATUS_RESPONSE_PROCESSING.getBytes(Charset.forName(CHARSET))));
        when(entityMock.getContentLength()).thenReturn(687L);
        when(response.getEntity()).thenReturn(entityMock);

        StatusResponse response = client.getStatus("submission-id", "customerkey", "accesstoken");
        assertNotNull(response);
        assertEquals("Processing", response.getStatusCode());
        assertEquals(null, response.getDownloadUrl());
    }

    @Test
    public void testGetStatusNull() throws IOException {
        when(entityMock.getContent()).thenReturn(new ByteArrayInputStream(STATUS_RESPONSE_NULL.getBytes(Charset.forName(CHARSET))));
        when(entityMock.getContentLength()).thenReturn(687L);
        when(response.getEntity()).thenReturn(entityMock);

        StatusResponse response = client.getStatus("submission-id", "customerkey", "accesstoken");
        assertNotNull(response);
        assertEquals(null, response.getStatusCode());
        assertEquals(null, response.getDownloadUrl());
    }

    @Test
    public void testGetMultipleStatus() throws IOException {
        when(entityMock.getContent())
                .thenReturn(new ByteArrayInputStream(STATUS_RESPONSE_PROCESSED.getBytes(Charset.forName(CHARSET))))
                .thenReturn(new ByteArrayInputStream(STATUS_RESPONSE_PROCESSED.getBytes(Charset.forName(CHARSET))))
                .thenReturn(new ByteArrayInputStream(STATUS_RESPONSE_PROCESSED.getBytes(Charset.forName(CHARSET))));
        when(entityMock.getContentLength()).thenReturn(687L);
        when(response.getEntity()).thenReturn(entityMock);

        List<StatusResponse> responses = client.getStatus(Arrays.asList(new String[]{"test", "test", "test"}), "customerkey", "accesstoken");
        assertNotNull(responses);
        assertEquals(3, responses.size());
        assertEquals("Processed", responses.get(0).getStatusCode());
        assertEquals("https://localhost/file/download/url", responses.get(0).getDownloadUrl());
        assertEquals("Processed", responses.get(1).getStatusCode());
        assertEquals("https://localhost/file/download/url", responses.get(1).getDownloadUrl());
        assertEquals("Processed", responses.get(2).getStatusCode());
        assertEquals("https://localhost/file/download/url", responses.get(2).getDownloadUrl());
    }

    @Test
    public void testDownloadFile() throws IOException {
        when(entityMock.getContent()).thenReturn(new ByteArrayInputStream("response file content".getBytes(Charset.forName(CHARSET))));
        when(entityMock.getContentLength()).thenReturn(21L);
        when(response.getEntity()).thenReturn(entityMock);

        DownloadResponse response = client.downloadFile("http://localhost/custom.csv", "ABC", System.getProperty("java.io.tmpdir"), "consumerKey");
        assertNotNull(response);
        assertEquals("ABC", response.getSubmissionId());
        assertEquals(200, response.getStatus());
        File f = new File(System.getProperty("java.io.tmpdir") + "\\custom.csv");
        assertEquals(f.getAbsolutePath(), response.getDownloadPath());
    }

    @Test
    public void testDownloadFiles() throws IOException {
        when(entityMock.getContent())
                .thenReturn(new ByteArrayInputStream("response file contentAAA".getBytes(Charset.forName(CHARSET))))
                .thenReturn(new ByteArrayInputStream("response file contentBBB".getBytes(Charset.forName(CHARSET))))
                .thenReturn(new ByteArrayInputStream("response file contentCCC".getBytes(Charset.forName(CHARSET))));
        doCallRealMethod().when(entityMock).writeTo(any(BufferedOutputStream.class));
        when(entityMock.getContentLength()).thenReturn(21L);
        when(response.getEntity()).thenReturn(entityMock);

        Map<String, String> m = new TreeMap<String, String>();
        m.put("http://localhost/test1.csv", "AAA");
        m.put("http://localhost/test2.csv", "BBB");
        m.put("http://localhost/test3.csv", "CCC");
        List<DownloadResponse> responses = client.downloadFiles(m, System.getProperty("java.io.tmpdir"), "consumerKey");
        assertNotNull(responses);
        assertEquals(3, responses.size());
        assertEquals("AAA", responses.get(0).getSubmissionId());
        assertEquals(200, responses.get(0).getStatus());

        File f = new File(System.getProperty("java.io.tmpdir") + "\\test1.csv");
        String content = readFile(f);
        assertNotNull(content);
        assertFalse(content.isEmpty());
        assertEquals("response file contentAAA", content);
        assertEquals(f.getAbsolutePath(), responses.get(0).getDownloadPath());
        assertEquals("BBB", responses.get(1).getSubmissionId());
        assertEquals(200, responses.get(1).getStatus());
        f.delete();

        f = new File(System.getProperty("java.io.tmpdir") + "\\test2.csv");
        content = readFile(f);
        assertNotNull(content);
        assertFalse(content.isEmpty());
        assertEquals("response file contentBBB", content);
        assertEquals(f.getAbsolutePath(), responses.get(1).getDownloadPath());
        assertEquals("CCC", responses.get(2).getSubmissionId());
        assertEquals(200, responses.get(2).getStatus());
        f.delete();

        f = new File(System.getProperty("java.io.tmpdir") + "\\test3.csv");
        content = readFile(f);
        assertNotNull(content);
        assertFalse(content.isEmpty());
        assertEquals("response file contentCCC", content);
        assertEquals(f.getAbsolutePath(), responses.get(2).getDownloadPath());
        f.delete();
    }

    @Test
    public void testGenerateSecretKey() {
        String key = client.generateCustomerKey();
        assertNotNull(key);
        assertFalse(key.isEmpty());
    }

    @Test(expected = RuntimeException.class)
    public void testDownloadPathNotDirectory() throws IOException {
        client.downloadFile("http://localhost/sample.csv", "submissionID", "./sample.csv", "customer key");
    }

    @Test
    public void testDownloadFileOutputStream() throws IOException {
        when(entityMock.getContent()).thenReturn(new ByteArrayInputStream("response file content".getBytes(Charset.forName(CHARSET))));
        doCallRealMethod().when(entityMock).writeTo(any(BufferedOutputStream.class));
        when(entityMock.getContentLength()).thenReturn(21L);
        when(response.getEntity()).thenReturn(entityMock);

        OutputStream os = client.downloadFile("http://localhost/custom.csv", "ABC", "consumerKey");
        String response = os.toString();
        assertEquals("response file content", response);
    }

    @Test
    public void testDownloadFilesOutputStream() throws IOException {
        when(entityMock.getContent())
                .thenReturn(new ByteArrayInputStream("response file contentAA".getBytes(Charset.forName(CHARSET))))
                .thenReturn(new ByteArrayInputStream("response file contentBB".getBytes(Charset.forName(CHARSET))))
                .thenReturn(new ByteArrayInputStream("response file contentCC".getBytes(Charset.forName(CHARSET))));
        doCallRealMethod().when(entityMock).writeTo(any(BufferedOutputStream.class));
        when(entityMock.getContentLength()).thenReturn(21L);
        when(response.getEntity()).thenReturn(entityMock);

        Map<String, String> m = new TreeMap<String, String>();
        m.put("http://localhost/test1.csv", "AAA");
        m.put("http://localhost/test2.csv", "BBB");
        m.put("http://localhost/test3.csv", "CCC");
        List<OutputStream> responses = client.downloadFiles(m, "consumerKey");
        assertNotNull(responses);
        assertEquals(3, responses.size());
        assertEquals("response file contentAA", responses.get(0).toString());
        assertEquals("response file contentBB", responses.get(1).toString());
        assertEquals("response file contentCC", responses.get(2).toString());
    }

    private String readFile(File f) throws IOException {
        String text = "";
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String line;
        while ((line = reader.readLine()) != null) {
            text += line;
        }
        reader.close();
        return text;
    }
}
