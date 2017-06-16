/**
 * Copyrighted as an unpublished work 2016 D&B.
 * Proprietary and Confidential.  Use, possession and disclosure subject to license agreement.
 * Unauthorized use, possession or disclosure is a violation of D&B's legal rights and may result
 * in suit or prosecution.
 */
package com.dnb.services.plus.sdk;

import org.apache.http.auth.AuthenticationException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * DirectPlus MultiRequest API client.
 *
 * @author Waldemar Sojka
 */
public interface MultiRequestClient {

    /**
     * Gets APIGEE access token to use with all APIGEE calls
     * @param accessKey provided access key
     * @param secretKey provided secret key
     * @return APIGEE access token
     * @throws IOException
     * @throws AuthenticationException
     */
    String getAccessToken(String accessKey, String secretKey) throws IOException, AuthenticationException;

    /**
     * Generates Base64 encoded AES256 key
     * @return Base64 encoded AES256 key
     */
    String generateCustomerKey();

    /**
     * Uploads file specified by path parameter to MultiRequest API
     *
     * @param accessToken APIGEE access token
     * @param customerKey customer key
     * @param path path to the file that will be uploaded
     * @return Object of type UploadResponse
     * @throws IOException
     */
    UploadResponse uploadFile(String accessToken, String customerKey, String path) throws IOException;

    /**
     * Gets signed AWS S3 URL for file upload
     * @param accessToken apigee access token
     * @param customerKey apigee customer key
     * @param path file to upload
     * @return Object of type JobDetails
     * @throws IOException
     */
    JobDetails getUploadUrl(String accessToken, String customerKey, String path) throws IOException;

    /**
     * Uploads file to given URL and returns HTTP status code
     * @param url Signed S3 URL for file upload
     * @param customerKey customed key
     * @param path file to upload
     * @return HTTP status code from upload
     * @throws IOException
     */
    int uploadFileToUrl(String url, String customerKey, String path) throws IOException;

    /**
     * Uploads multiple files specified by paths parameter
     *
     * @param accessToken APIGEE access token
     * @param customerKey customer key
     * @param paths list of paths to files that should be uploaded
     * @return list with objects of type UploadResponse
     * @throws IOException
     */
    List<UploadResponse> uploadFiles(String accessToken, String customerKey, List<String> paths) throws IOException;

    /**
     * Gets status of given submission id
     *
     * @param submissionId submission id (as received from uploadFile method response)
     * @param customerKey customer key
     * @param accessToken APIGEE access token
     * @return Object of type StatusResponse
     * @throws IOException
     */
    StatusResponse getStatus(String submissionId, String customerKey, String accessToken) throws IOException;

    /**
     * Gets status of given submission ids
     *
     * @param submissionIds list of submission ids to check
     * @param customerKey customer key
     * @param accessToken APIGEE access token
     * @return list with objects of type StatusResponse
     * @throws IOException
     */
    List<StatusResponse> getStatus(List<String> submissionIds, String customerKey, String accessToken) throws IOException;

    /**
     * Downloads given file and saves it under given path.
     *
     * @param downloadUrl URL of file to download
     * @param folderPath directory path (without the filename) where to store downloaded file
     * @param customerKey customer key
     * @return object of type DownloadResponse
     * @throws IOException
     */
    DownloadResponse downloadFile(String downloadUrl, String submissionId, String folderPath, String customerKey) throws IOException;

    /**
     * Downloads file from Amazon S3
     * @param downloadUrl download URL of results file
     * @param submissionId ID of given job
     * @param customerKey customer key
     * @return
     * @throws IOException
     */
    OutputStream downloadFile(String downloadUrl, String submissionId, String customerKey) throws IOException;

    /**
     *
     * @param toDownload map that contains downloadUrs and submissionIDs where submissionID is a key, and download URL
     *                   is the value
     * @param folderPath
     * @param customerKey
     * @return
     * @throws IOException
     */
    List<DownloadResponse> downloadFiles(Map<String, String> toDownload, String folderPath, String customerKey) throws IOException;

    /**
     *
     * @param downloadUrls
     * @param consumerKey
     * @return
     * @throws IOException
     */
    /**
     * Downloads files specified inside toDownload map
     * @param toDownload map that contains download urls and submission IDs where submission ID is a key, and
     *                   download URL is a value
     * @param  customerKey
     * @return
     * @throws IOException
     */
    List<OutputStream> downloadFiles(Map<String, String> toDownload, String customerKey) throws IOException;
}
