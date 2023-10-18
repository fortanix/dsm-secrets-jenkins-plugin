/* Copyright (c) Fortanix, Inc.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.fortanix.jenkins;

import com.fortanix.sdkms.v1.ApiClient;
import com.fortanix.sdkms.v1.ApiException;
import com.fortanix.sdkms.v1.api.AuthenticationApi;
import com.fortanix.sdkms.v1.api.SecurityObjectsApi;
import com.fortanix.sdkms.v1.auth.ApiKeyAuth;
import com.fortanix.sdkms.v1.model.AuthResponse;
import com.fortanix.sdkms.v1.model.KeyObject;
import com.fortanix.sdkms.v1.model.KeyOperations;
import com.fortanix.sdkms.v1.model.ObjectType;
import java.nio.charset.StandardCharsets;
import java.security.ProviderException;
import java.util.Base64;
import java.util.List;
import javax.annotation.CheckForNull;
import org.kohsuke.stapler.DataBoundConstructor;

public class SecretService {

    ApiClient apiClient;
    private AuthenticationApi authenticationApi;

    @DataBoundConstructor
    public SecretService(@CheckForNull ApiClient apiClient) {
        this.apiClient = apiClient;
        this.authenticationApi = new AuthenticationApi(this.apiClient);
    }

    public void setup() throws ProviderException {
        AuthResponse authResponse = null;
        try {
            authResponse = this.authenticationApi.authorize();
        } catch (ApiException e) {
            throw new ProviderException(e.getMessage());
        }
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) this.apiClient.getAuthentication("bearerToken");
        apiKeyAuth.setApiKey(authResponse.getAccessToken());
        apiKeyAuth.setApiKeyPrefix("Bearer");
    }

    public String getSecret(String secretPath) {

        SecurityObjectsApi sObjectsApi = new SecurityObjectsApi();
        try {
            List<KeyObject> allObjects = sObjectsApi.getSecurityObjects(secretPath, null, null);
            if (allObjects == null || allObjects.isEmpty()) return null;
            KeyObject sobject = allObjects.get(0);
            if (sobject.getKeyOps().contains(KeyOperations.EXPORT)) {

                KeyObject exportedSobj = sObjectsApi.getSecurityObjectValue(sobject.getKid());
                if (sobject.getObjType() == ObjectType.SECRET || sobject.getObjType() == ObjectType.OPAQUE) {
                    try {
                        String secretStr = new String(exportedSobj.getValue(), StandardCharsets.UTF_8);
                        return secretStr;
                    } catch (Exception e) {
                        return "";
                    }
                } else {
                    return Base64.getEncoder().encodeToString(exportedSobj.getValue());
                }
            }
        } catch (ApiException e) {
            throw new ProviderException(e.getMessage());
        }
        return "";
    }

    public void shutdown() {
        try {
            this.authenticationApi.terminate();
        } catch (ApiException e) {
            throw new ProviderException(e.getMessage());
        }
    }
}
