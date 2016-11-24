package uk.gov.register;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayFactory;
import uk.gov.organisation.client.GovukClientConfiguration;
import uk.gov.register.auth.AuthenticatorConfiguration;
import uk.gov.register.auth.RegisterAuthenticatorFactory;
import uk.gov.register.configuration.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;

public class RegisterConfiguration extends Configuration
        implements AuthenticatorConfiguration,
        RegisterNameConfiguration,
        RegisterDomainConfiguration,
        RegisterContentPagesConfiguration,
        ResourceConfiguration,
        GovukClientConfiguration,
        RegisterTrackingConfiguration {
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

    @SuppressWarnings("unused")
    @Valid
    @JsonProperty
    private Optional<String> trackingId = Optional.empty();

    @SuppressWarnings("unused")
    @Valid
    @JsonProperty
    private FlywayFactory flywayFactory = new FlywayFactory();

    public DataSourceFactory getDatabase() {
        return database;
    }

    public FlywayFactory getFlywayFactory() {
        flywayFactory.setBaselineOnMigrate(true);
        flywayFactory.setLocations(Collections.singletonList("/sql"));
        flywayFactory.setPlaceholders(Collections.singletonMap("registerName", getRegisterName()));
        flywayFactory.setOutOfOrder(true);
        return flywayFactory;
    }

    public String getRegisterName() {
        return register;
    }

    @Override
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

    public Optional<String> getRegisterTrackingId() {
        return trackingId;
    }
}
