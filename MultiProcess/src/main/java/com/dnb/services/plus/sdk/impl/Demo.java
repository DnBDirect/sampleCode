/**
 * Copyrighted as an unpublished work 2016 D&B.
 * Proprietary and Confidential.  Use, possession and disclosure subject to license agreement.
 * Unauthorized use, possession or disclosure is a violation of D&B's legal rights and may result
 * in suit or prosecution.
 */
package com.dnb.services.plus.sdk.impl;

import com.dnb.services.plus.sdk.DownloadResponse;
import com.dnb.services.plus.sdk.JobDetails;
import com.dnb.services.plus.sdk.StatusResponse;

public class Demo {

    public static void main(String args[]) throws Exception {
        MultiRequestClientImpl client = new MultiRequestClientImpl();
        String accessToken = client.getAccessToken("access_key", "secret_key");
        String key = client.generateCustomerKey();
        String filename = "sample.csv";

        /* submit request for new job */
        JobDetails job = client.getUploadUrl(accessToken, key, filename);
        System.out.println("Job ID: " + job.getJobId());
        StatusResponse jobStatus = client.getStatus(job.getJobId(), key, accessToken);
        System.out.println("Job status: " + jobStatus.getStatusCode());

        /* upload file to AWS S3 */
        client.uploadFileToUrl(job.getUploadUrl(), key, filename);

        /* wait until file is fully processed */
        while (!jobStatus.getStatusCode().equals("Processed")) {
            Thread.sleep(10000);
            try {
                jobStatus = client.getStatus(job.getJobId(), key, accessToken);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Download URL: " + jobStatus.getDownloadUrl());

        /* download results file */
        DownloadResponse download = client.downloadFile(jobStatus.getDownloadUrl(), job.getJobId(), "./", key);
        System.out.println(download.getStatus());
        System.out.println(download.getDownloadPath().toString());
    }
}
