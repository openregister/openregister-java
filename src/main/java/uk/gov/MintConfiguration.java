package uk.gov;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;
import uk.gov.mint.auth.MintAuthenticatorFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Optional;

public class MintConfiguration extends Configuration {
    @SuppressWarnings("unused")
    @Valid
    @NotNull
    @JsonProperty
    private DataSourceFactory database;

    @Valid
    @JsonProperty
    private MintAuthenticatorFactory credentials = new MintAuthenticatorFactory();

    @SuppressWarnings("unused")
    @Valid
    @NotNull
    @JsonProperty
    private String register;

    @SuppressWarnings("unused")
    @Valid
    @JsonProperty
    private String ctserver;

    @Valid
    @NotNull
    private JerseyClientConfiguration jerseyClientConfiguration
            = new JerseyClientConfiguration();

    @JsonProperty("jerseyClient")
    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return jerseyClientConfiguration;
    }

    public DataSourceFactory getDatabase() {
        return database;
    }

    public String getRegister() {
        return register;
    }

    public MintAuthenticatorFactory getAuthenticator() {
        return credentials;
    }

    public Optional<String> getCTServer() { return Optional.ofNullable(ctserver); }
}
