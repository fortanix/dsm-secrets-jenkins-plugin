/* Copyright (c) Fortanix, Inc.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.fortanix.jenkins.configuration;

import hudson.Extension;
import hudson.model.Item;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class FortanixGlobalConfiguration extends GlobalConfiguration {
    private FortanixConfiguration configuration;

    @Nonnull
    public static FortanixGlobalConfiguration get() {
        FortanixGlobalConfiguration instance = GlobalConfiguration.all().get(FortanixGlobalConfiguration.class);
        if (instance == null) {
            throw new IllegalStateException();
        }
        return instance;
    }

    public FortanixGlobalConfiguration() {
        load();
    }

    public FortanixConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this, json);
        return true;
    }

    @DataBoundSetter
    public void setConfiguration(FortanixConfiguration configuration) {
        this.configuration = configuration;
        save();
    }

    @Extension
    public static class ForJob extends FortanixConfigResolver {

        @Nonnull
        @Override
        public FortanixConfiguration forJob(@Nonnull Item job) {
            LOGGER.log(Level.ALL, "forJob");
            return FortanixGlobalConfiguration.get().getConfiguration();
        }
        private static final Logger LOGGER = Logger.getLogger(ForJob.class.getName());
    }
    
    private static final Logger LOGGER = Logger.getLogger(FortanixGlobalConfiguration.class.getName());
}
