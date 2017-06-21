/**
 * Copyrighted as an unpublished work 2016 D&B.
 * Proprietary and Confidential.  Use, possession and disclosure subject to license agreement.
 * Unauthorized use, possession or disclosure is a violation of D&B's legal rights and may result
 * in suit or prosecution.
 */
package com.dnb.services.plus.sdk.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author SojkaW
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class RequestEndpointResponse {

    private String jobID;

    private JobSubmissionDetail jobSubmissionDetail;

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public JobSubmissionDetail getJobSubmissionDetail() {
        return jobSubmissionDetail;
    }

    public void setJobSubmissionDetail(JobSubmissionDetail jobSubmissionDetail) {
        this.jobSubmissionDetail = jobSubmissionDetail;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JobSubmissionDetail {

        private String contentURL;

        public String getContentURL() {
            return contentURL;
        }

        public void setContentURL(String contentURL) {
            this.contentURL = contentURL;
        }
    }
}

