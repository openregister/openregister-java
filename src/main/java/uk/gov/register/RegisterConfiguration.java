package uk.gov.register;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;
import uk.gov.organisation.client.GovukClientConfiguration;
import uk.gov.register.auth.RegisterAuthenticatorFactory;
import uk.gov.register.configuration.RegisterContentPagesConfiguration;
import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.configuration.ResourceConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Optional;

public class RegisterConfiguration extends Configuration implements RegisterNameConfiguration,
        RegisterDomainConfiguration,
        RegisterContentPagesConfiguration,
        ResourceConfiguration,
        GovukClientConfiguration {
    @Valid
    @NotNull
    @JsonProperty
    private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

    @SuppressWarnings("unused")
    @Valid
    @NotNull
    @JsonProperty
    private DataSourceFactory database;

    @Valid
    @NotNull
    @JsonProperty
    private String registerDomain = "openregister.org";

    @Valid
    @JsonProperty
    private RegisterAuthenticatorFactory credentials = new RegisterAuthenticatorFactory();

    @SuppressWarnings("unused")
    @Valid
    @NotNull
    @JsonProperty
    private String register;

    @SuppressWarnings("unused")
    @Valid
    @JsonProperty
    private String cloudWatchEnvironmentName;

    @Override
    public String getRegisterDomain() {
        return registerDomain;
    }

    @Valid
    @JsonProperty
    private boolean enableDownloadResource = false;

    @SuppressWarnings("unused")
    @Valid
    @JsonProperty
    private Optional<String> historyPageUrl = Optional.empty();

    public DataSourceFactory getDatabase() {
        return database;
    }

    public String getRegister() {
        return register;
    }

    public RegisterAuthenticatorFactory getAuthenticator() {
        return credentials;
    }

    public Optional<String> cloudWatchEnvironmentName() {
        return Optional.ofNullable(cloudWatchEnvironmentName);
    }

    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return jerseyClient;
    }

    @Override
    public URI getGovukEndpoint() {
        return URI.create("https://www.gov.uk");
    }

    @Override
    public boolean getEnableDownloadResource() {
        return enableDownloadResource;
    }

    public Optional<String> getRegisterHistoryPageUrl() {
        return historyPageUrl;
    }
}
