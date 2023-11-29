/* Copyright (c) Fortanix, Inc.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.fortanix.jenkins.configuration;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.hudson.plugins.folder.AbstractFolderPropertyDescriptor;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.kohsuke.stapler.DataBoundConstructor;

public class FortanixFolderConfiguration extends AbstractFolderProperty<AbstractFolder<?>> {
    private final FortanixConfiguration configuration;

    public FortanixFolderConfiguration() {
        this.configuration = null;
    }

    @DataBoundConstructor
    public FortanixFolderConfiguration(FortanixConfiguration configuration) {
        this.configuration = configuration;
    }

    public FortanixConfiguration getConfiguration() {
        return configuration;
    }

    @Extension
    public static class DescriptorImpl extends AbstractFolderPropertyDescriptor {}

    @Extension(ordinal = 100)
    public static class ForJob extends FortanixConfigResolver {
        @Nonnull
        @Override
        public FortanixConfiguration forJob(@Nonnull Item job) {
            LOGGER.log(Level.ALL, "forJob");
            FortanixConfiguration resultingConfig = null;
            for (ItemGroup g = job.getParent(); g instanceof AbstractFolder; g = ((AbstractFolder) g).getParent()) {
                FortanixFolderConfiguration folderProperty =
                        ((AbstractFolder<?>) g).getProperties().get(FortanixFolderConfiguration.class);
                if (folderProperty == null) {
                    continue;
                }
                if (resultingConfig != null) {
                    resultingConfig = resultingConfig.mergeWithParent(folderProperty.getConfiguration());
                } else {
                    resultingConfig = folderProperty.getConfiguration();
                }
            }

            return resultingConfig;
        }

        private static final Logger LOGGER = Logger.getLogger(ForJob.class.getName());
    }

    private static final Logger LOGGER = Logger.getLogger(FortanixFolderConfiguration.class.getName());
}
