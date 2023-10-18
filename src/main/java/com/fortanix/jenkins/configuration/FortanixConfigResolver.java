/* Copyright (c) Fortanix, Inc.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.fortanix.jenkins.configuration;

import hudson.ExtensionPoint;
import hudson.model.Item;
import javax.annotation.Nonnull;

public abstract class FortanixConfigResolver implements ExtensionPoint {

    public abstract @Nonnull FortanixConfiguration forJob(@Nonnull Item job);
}
