/* Copyright (c) Fortanix, Inc.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.fortanix.jenkins.configuration;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;

import com.fortanix.jenkins.credentials.FortanixCredentials;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FortanixConfiguration extends AbstractDescribableImpl<FortanixConfiguration> implements Serializable {

    private String ftxCredentialId;

    public FortanixConfiguration() {
    }

    /**
     * Constructor bound to config.jelly to create a new Configuration based on the auth credential to the vault
     */
    @DataBoundConstructor
    public FortanixConfiguration(String ftxCredentialId) {
        LOGGER.log(Level.ALL, "Constructor ftxCredentialId: "+ftxCredentialId);
        this.ftxCredentialId = ftxCredentialId;
    }

    public String getFortanixCredentialId() {
        return ftxCredentialId;
    }

    @DataBoundSetter
    public void setFortanixCredentialId(String ftxCredentialId) {
        this.ftxCredentialId = ftxCredentialId;
    }

    /**
     * Create a new config and pull the parent auth credential if the current config does not have
     * the auth credential set
     *
     * @param parent parent FortanixConfiguration
     * @return new FortanixConfiguration
     */
    public FortanixConfiguration mergeWithParent(FortanixConfiguration parent) {
        LOGGER.log(Level.ALL, "Merge with Parent");
        if (parent == null) {
            LOGGER.log(Level.ALL, "Return This: "+ftxCredentialId);
            return this;
        }
        FortanixConfiguration result = new FortanixConfiguration(this.getFortanixCredentialId());
        LOGGER.log(Level.ALL, "Credential ID: "+result.getFortanixCredentialId());
        if (StringUtils.isBlank(result.getFortanixCredentialId())) {
            result.setFortanixCredentialId(parent.getFortanixCredentialId());
        }
        return result;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<FortanixConfiguration> {
        @Override
        public String getDisplayName() {
            return "Fortanix DSM Secrets Configuration";
        }

        /**
         * Populates dropdown in config jelly for credential choice during client auth.
         */
        public ListBoxModel doFillFtxCredentialIdItems(@AncestorInPath Item item, @QueryParameter String uri) {
            List<DomainRequirement> domainRequirements = URIRequirementBuilder.fromUri(uri).build();
            return new StandardListBoxModel().includeEmptyValue().includeAs(ACL.SYSTEM, item,
                    FortanixCredentials.class, domainRequirements);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(FortanixConfiguration.class.getName());
}
