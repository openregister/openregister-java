package uk.gov.register.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.Optional;

public class RegisterAuthenticatorFactory {
    @Valid
    @JsonProperty
    private String user;

    @Valid
    @JsonProperty
    private String password;

    public Optional<RegisterAuthenticator> build() {
        if (user != null && password != null) {
            return Optional.of(new RegisterAuthenticator(user, password));
        }
        return Optional.empty();
    }
}
