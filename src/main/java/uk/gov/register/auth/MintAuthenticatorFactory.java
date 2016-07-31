package uk.gov.register.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.Optional;

public class MintAuthenticatorFactory {
    @Valid
    @JsonProperty
    private String user;

    @Valid
    @JsonProperty
    private String password;

    public Optional<MintAuthenticator> build() {
        if (user != null && password != null) {
            return Optional.of(new MintAuthenticator(user, password));
        }
        return Optional.empty();
    }
}
