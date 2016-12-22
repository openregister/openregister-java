package uk.gov.register.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import uk.gov.register.auth.RegisterAuthenticator.User;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class RegisterAuthenticatorFactory {
    @Valid
    @NotNull
    @JsonProperty
    private String user;

    @Valid
    @NotNull
    @JsonProperty
    private String password;

    public RegisterAuthenticator buildAuthenticator() {
        return new RegisterAuthenticator(user, password);
    }

}
