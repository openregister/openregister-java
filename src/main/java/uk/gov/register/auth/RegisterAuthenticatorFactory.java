package uk.gov.register.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;

import javax.validation.Valid;
import java.util.Optional;

public class RegisterAuthenticatorFactory {
    @Valid
    @JsonProperty
    private String user;

    @Valid
    @JsonProperty
    private String password;

    public Optional<AuthDynamicFeature> build() {
        if (user != null && password != null) {
            RegisterAuthenticator registerAuthenticator = new RegisterAuthenticator(user, password);
            AuthDynamicFeature authFeature = new AuthDynamicFeature(
                    new BasicCredentialAuthFilter.Builder<RegisterAuthenticator.User>()
                            .setAuthenticator(registerAuthenticator)
                            .buildAuthFilter()
            );
            return Optional.of(authFeature);
        }
        return Optional.empty();
    }

}
