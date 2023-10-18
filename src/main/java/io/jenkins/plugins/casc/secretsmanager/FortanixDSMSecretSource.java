package io.jenkins.plugins.casc.secretsmanager;

import com.fortanix.sdkms.v1.ApiClient;
import com.fortanix.sdkms.v1.ApiException;
import com.fortanix.sdkms.v1.api.AuthenticationApi;
import com.fortanix.sdkms.v1.api.SecurityObjectsApi;
import com.fortanix.sdkms.v1.auth.ApiKeyAuth;
import com.fortanix.sdkms.v1.model.AuthResponse;
import com.fortanix.sdkms.v1.model.KeyObject;
import com.fortanix.sdkms.v1.model.SobjectDescriptor;
import hudson.Extension;
import io.jenkins.plugins.casc.SecretSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@Extension
public class FortanixDSMSecretSource extends SecretSource {

    private static final Logger LOG = Logger.getLogger(FortanixDSMSecretSource.class.getName());

    private static final String DSM_SERVICE_ENDPOINT = "DSM_SERVICE_ENDPOINT";
    private static final String DSM_API_KEY = "DSM_API_KEY";

    private transient SecurityObjectsApi client = null;

    private final Pattern UUID_REGEX =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    @Override
    public Optional<String> reveal(String id) throws IOException {
        try {
            SobjectDescriptor soDescriptor = UUID_REGEX.matcher(id).matches()
                    ? new SobjectDescriptor().kid(id)
                    : new SobjectDescriptor().name(id);
            final KeyObject result = client.getSecurityObjectValueEx(soDescriptor);

            return Optional.ofNullable(new String(result.getValue(), StandardCharsets.UTF_8));
        } catch (ApiException e) {
            LOG.info(e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void init() {
        try {
            client = createClient();
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    private static SecurityObjectsApi createClient() throws ApiException {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(getServiceEndpoint());
        apiClient.setBasicAuthString(getApiKey());
        apiClient.setApiKey(getApiKey());
        AuthenticationApi authenticationApi = new AuthenticationApi(apiClient);
        AuthResponse authResponse = authenticationApi.authorize();
        ApiKeyAuth bearerTokenAuth = (ApiKeyAuth) apiClient.getAuthentication("bearerToken");
        bearerTokenAuth.setApiKey(authResponse.getAccessToken());
        bearerTokenAuth.setApiKeyPrefix("Bearer");

        return new SecurityObjectsApi(apiClient);
    }

    private static String getServiceEndpoint() {
        return System.getenv(DSM_SERVICE_ENDPOINT);
    }

    private static String getApiKey() {
        return System.getenv(DSM_API_KEY);
    }
}
