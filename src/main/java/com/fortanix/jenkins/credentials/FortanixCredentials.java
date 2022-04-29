/* Copyright (c) Fortanix, Inc.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.fortanix.jenkins.credentials;

import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import com.cloudbees.plugins.credentials.NameWith;
import com.cloudbees.plugins.credentials.common.StandardCredentials;

import com.fortanix.sdkms.v1.ApiClient;

import javax.annotation.Nonnull;
import java.io.Serializable;

@NameWith(FortanixCredentials.NameProvider.class)
public interface FortanixCredentials extends StandardCredentials, Serializable {

    ApiClient getDSMClient();

    class NameProvider extends CredentialsNameProvider<FortanixCredentials> {

        @Nonnull
        public String getName(@Nonnull FortanixCredentials credentials) {
            return credentials.getDescription();
        }
    }
}
