package uk.gov.register.auth;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import uk.gov.mint.User;

import java.util.Optional;

public class RegisterAuthenticator implements Authenticator<BasicCredentials, User> {
    private final String expectedUsername;
    private final String expectedPassword;

    public RegisterAuthenticator(String expectedUsername, String expectedPassword) {
        this.expectedUsername = expectedUsername;
        this.expectedPassword = expectedPassword;
    }

    @Override
    public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
        if (credentials.getUsername().equals(expectedUsername) && credentials.getPassword().equals(expectedPassword)) {
            return Optional.of(new User());
        }
        return Optional.empty();
    }
}
