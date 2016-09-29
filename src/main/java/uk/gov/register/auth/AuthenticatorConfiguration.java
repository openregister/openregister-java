package uk.gov.register.auth;

public interface AuthenticatorConfiguration {
    RegisterAuthenticatorFactory getAuthenticator();
}
