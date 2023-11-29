/* Copyright (c) Fortanix, Inc.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.fortanix.jenkins.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

public class FortanixSecret extends AbstractDescribableImpl<FortanixSecret> {

    private String path;
    private String envVar;

    @DataBoundConstructor
    public FortanixSecret(String path, String envVar) {
        this.path = path;
        this.envVar = envVar;
    }

    public String getEnvVar() {
        return envVar;
    }

    public String getPath() {
        return path;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<FortanixSecret> {

        @Override
        public String getDisplayName() {
            return "Fortanix DSM Secret";
        }
    }
}
