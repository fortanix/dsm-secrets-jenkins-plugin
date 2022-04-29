/* Copyright (c) Fortanix, Inc.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.fortanix.jenkins.credentials;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;

import com.fortanix.sdkms.v1.ApiClient;
import com.fortanix.sdkms.v1.ApiException;
import com.fortanix.sdkms.v1.Configuration;

import hudson.Extension;
import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client Credentials class used for authenticating to Fortanix DSM and retrieve secrets at build execution time.
 */
public class ClientCredentials extends BaseStandardCredentials implements FortanixCredentials {

    private final @Nonnull
    Secret apiKey;

    private final @Nonnull
    String apiEndpoint;

    @DataBoundConstructor
    public ClientCredentials(@CheckForNull CredentialsScope scope, @CheckForNull String id, @CheckForNull String description, @Nonnull String apiEndpoint, @Nonnull Secret apiKey) {
        super(scope, id, description);
        this.apiKey = apiKey;
        this.apiEndpoint = apiEndpoint;
    }

    public String getEndpoint() {
        return apiEndpoint;
    }

    public Secret getApiKey() {
        return apiKey;
    }

    @Override
    public ApiClient getDSMClient() {
        LOGGER.log(Level.ALL, "GetDSMClient");
        ApiClient client = new ApiClient();
        Configuration.setDefaultApiClient(client);

        client.setBasePath(this.apiEndpoint);
        client.setBasicAuthString(this.apiKey.getPlainText());
        return client;
    }

    @Extension
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {

        @Override
        public String getDisplayName() {
            return "Fortanix DSM Client Credentials";
        }

    }

    private static final Logger LOGGER = Logger.getLogger(ClientCredentials.class.getName());
}
