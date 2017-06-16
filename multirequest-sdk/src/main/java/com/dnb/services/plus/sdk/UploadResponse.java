/**
 * Copyrighted as an unpublished work 2016 D&B.
 * Proprietary and Confidential.  Use, possession and disclosure subject to license agreement.
 * Unauthorized use, possession or disclosure is a violation of D&B's legal rights and may result
 * in suit or prosecution.
 */
package com.dnb.services.plus.sdk;

/**
 * @author SojkaW
 */
public class UploadResponse {

    private int statusCode;

    private String submissionId;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }
}
