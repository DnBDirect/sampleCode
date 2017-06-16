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
public class StatusResponse {

    private String statusCode;

    private String downloadUrl;

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
