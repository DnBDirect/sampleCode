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
class StatusEndpointResponse {

    private Information information;

    private OutputDetail outputDetail;

    public Information getInformation() {
        return information;
    }

    public void setInformation(Information information) {
        this.information = information;
    }

    public OutputDetail getOutputDetail() {
        return outputDetail;
    }

    public void setOutputDetail(OutputDetail outputDetail) {
        this.outputDetail = outputDetail;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OutputDetail {
        private String contentURL;

        public String getContentURL() {
            return contentURL;
        }

        public void setContentURL(String contentURL) {
            this.contentURL = contentURL;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Information {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

