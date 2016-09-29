package uk.gov.register.auth;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class AuthBundle implements ConfiguredBundle<AuthenticatorConfiguration> {
    @Override
    public void initialize(Bootstrap<?> bootstrap) { }

    @Override
    public void run(AuthenticatorConfiguration configuration, Environment environment) throws Exception {
        configuration.getAuthenticator().build()
                .ifPresent(authFeature -> environment.jersey().register(authFeature));
    }
}
