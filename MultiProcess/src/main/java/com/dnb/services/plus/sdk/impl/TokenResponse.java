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
class TokenResponse {

    private String access_token;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }
}
