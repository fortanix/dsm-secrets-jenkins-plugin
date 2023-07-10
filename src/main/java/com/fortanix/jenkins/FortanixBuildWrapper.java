/* Copyright (c) Fortanix, Inc.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.fortanix.jenkins;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.matchers.IdMatcher;

import com.fortanix.jenkins.configuration.FortanixConfigResolver;
import com.fortanix.jenkins.configuration.FortanixConfiguration;
import com.fortanix.jenkins.credentials.FortanixCredentials;
import com.fortanix.jenkins.model.FortanixSecret;
import com.fortanix.jenkins.SecretService;

import com.fortanix.sdkms.v1.ApiException;

import hudson.*;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.tasks.BuildWrapperDescriptor;
import jenkins.tasks.SimpleBuildWrapper;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.io.PrintStream;
import java.security.ProviderException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class FortanixBuildWrapper extends SimpleBuildWrapper {

    private List<FortanixSecret> fortanixSecrets;
    private FortanixConfiguration configuration;

    @DataBoundConstructor
    public FortanixBuildWrapper(@CheckForNull List<FortanixSecret> fortanixSecrets) {
        if (fortanixSecrets != null) {
            LOGGER.log(Level.INFO, "Constructor: #" + fortanixSecrets.size());
            this.fortanixSecrets = fortanixSecrets;
        } else {
            LOGGER.log(Level.WARNING, "Constructor: NULL");
        }
    }

    @Override
    public void setUp(Context context, Run<?, ?> build, FilePath filePath, Launcher launcher, TaskListener taskListener, EnvVars envVars) throws IOException, InterruptedException {
        LOGGER.log(Level.ALL, "Setup");
        PrintStream logger = taskListener.getLogger();
        updateConfig(build);
        FortanixCredentials credential = getCredentials(build);

        if (fortanixSecrets != null && !fortanixSecrets.isEmpty()) {
            try {
                populateSecrets(context, credential);
            } catch (Exception e) {
                e.printStackTrace(logger);
                throw new AbortException(e.getMessage());
            }
        } else {
            if (this.fortanixSecrets == null) {
                LOGGER.log(Level.WARNING, "Setup fortanixSecrets NULL");
            } else {
                LOGGER.log(Level.INFO, "Setup fortanixSecrets EMPTY");
            }
        }
    }

    public List<FortanixSecret> getFortanixSecrets() {
        return fortanixSecrets;
    }

    @DataBoundSetter
    public void setConfiguration(FortanixConfiguration configuration) {
        LOGGER.log(Level.ALL, "Set Configuration: "+configuration);
        this.configuration = configuration;
    }

    public FortanixConfiguration getConfiguration() {
        LOGGER.log(Level.ALL, "Get Configuration");
        return configuration;
    }


    private FortanixCredentials getCredentials(Run build) {
        String id = getConfiguration().getftxCredentialId();
        LOGGER.log(Level.INFO, "Get Credentials ID: "+ id);
        if (StringUtils.isBlank(id)) {
            throw new RuntimeException("The credential id was not configured - please specify the credentials to use.");
        }
        List<FortanixCredentials> credentials = CredentialsProvider.lookupCredentials(FortanixCredentials.class, build.getParent(), ACL.SYSTEM, Collections.<DomainRequirement>emptyList());
        FortanixCredentials credential = CredentialsMatchers.firstOrNull(credentials, new IdMatcher(id));

        if (credential == null) {
            throw new RuntimeException("No credential exists that matches the configured credential id.");
        }

        return credential;
    }

    private void populateSecrets(Context context, FortanixCredentials credentials) {

        LOGGER.log(Level.INFO, "Populate Secrets");
        SecretService secretService = new SecretService(credentials.getDSMClient());
        try {
            secretService.setup();

            for (FortanixSecret secret : this.fortanixSecrets) {
                String secretVal = secretService.getSecret(secret.getPath());
                String env = secret.getEnvVar();
                LOGGER.log(Level.INFO, "Env: "+env);
                context.env(env, secretVal);
            }
            secretService.shutdown();

        } catch (ProviderException e) {
            throw new RuntimeException("Exception calling Fortanix DSM Secrets API", e);
        }
    }

    private void updateConfig(Run<?, ?> build) {
        LOGGER.log(Level.ALL, "UpdateConfig");
        for (FortanixConfigResolver resolver : ExtensionList.lookup(FortanixConfigResolver.class)) {
            if (configuration != null) {
                configuration = configuration.mergeWithParent(resolver.forJob(build.getParent()));
                LOGGER.log(Level.ALL, "UpdateConfig - current");
            } else {
                configuration = resolver.forJob(build.getParent());
                LOGGER.log(Level.ALL, "UpdateConfig - parent");
            }
        }
        if (configuration == null) {
            throw new RuntimeException("No configuration found - please configure the Fortanix DSM Secrets plugin.");
        }
    }

    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {
        public DescriptorImpl() {
            super(FortanixBuildWrapper.class);
            load();
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        @Override
        public String getDisplayName() {
            return "Fortanix DSM Secrets";
        }
    }

    private static final Logger LOGGER = Logger.getLogger(FortanixBuildWrapper.class.getName());
}
