package com.fortanix.jenkins.credentials;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.credentialsbinding.BindingDescriptor;
import org.jenkinsci.plugins.credentialsbinding.MultiBinding;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.fortanix.jenkins.SecretService;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;

public class FortanixCredentialsBinding extends MultiBinding<ClientCredentials> {
    
    public static final String DEFAULT_ENVIRONMENT_VARIABLE_NAME = "FTX_VAR";
    private String environmentVariable;
    private String path;
    

    @DataBoundConstructor
    public FortanixCredentialsBinding(String credentialsId) {
        super(credentialsId);
    }

    @Override
    protected Class<ClientCredentials> type() {
        return ClientCredentials.class;
    }

    @DataBoundSetter
    public void setPath(String path) {
        this.path = path;
    }

    public String getPath(){
        return this.path;
    }

    @DataBoundSetter
    public void setEnvironmentVariable(String environmentVariable) {
        this.environmentVariable = environmentVariable;
    }

    public String getEnvironmentVariable(){
        if (!StringUtils.isBlank(environmentVariable)) {
            return environmentVariable;
        }
        return DEFAULT_ENVIRONMENT_VARIABLE_NAME;
    }

    @Override
    public MultiEnvironment bind(@Nonnull Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
        ClientCredentials credentials = this.getCredentials(build);
        SecretService sc = new SecretService(credentials.getDSMClient());
        sc.setup();
        Map<String, String> map = new HashMap<String, String>();
        map.put(this.getEnvironmentVariable(), sc.getSecret(this.path));
        return new MultiEnvironment(map);
    }

    @Override
    public Set<String> variables() {
        Set<String> variables = new HashSet<String>();
        variables.add(this.getEnvironmentVariable());
        return variables;
    }

    @Symbol("fortanixDsmSecret")
    @Extension
    public static class DescriptorImpl extends BindingDescriptor<ClientCredentials> {

        @Override
        protected Class<ClientCredentials> type() {
            return ClientCredentials.class;
        }

        @Override
        public String getDisplayName() {
            return "Fortanix DSM credentials";
        }
        
        public String getDefaultEnvironmentVariable() {
            return DEFAULT_ENVIRONMENT_VARIABLE_NAME;
        }
    }
}
