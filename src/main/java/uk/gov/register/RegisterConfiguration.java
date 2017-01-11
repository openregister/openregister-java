package uk.gov.register;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayFactory;
import uk.gov.organisation.client.GovukClientConfiguration;
import uk.gov.register.auth.RegisterAuthenticatorFactory;
import uk.gov.register.configuration.RegisterConfigConfiguration;
import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.core.AllTheRegistersFactory;
import uk.gov.register.core.RegisterContextFactory;
import uk.gov.register.core.RegisterName;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterConfiguration extends Configuration
        implements RegisterDomainConfiguration,
        RegisterConfigConfiguration,
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
    private Optional<String> custodianName = Optional.empty();

    @Valid
    @JsonProperty
    private List<String> similarRegisters = emptyList();

    @Valid
    @NotNull
    @JsonProperty
    private RegisterAuthenticatorFactory credentials = new RegisterAuthenticatorFactory();

    @SuppressWarnings("unused")
    @Valid
    @NotNull
    @JsonProperty
    private RegisterName register;

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

    @Valid
    @JsonProperty
    private boolean enableRegisterDataDelete = false;

    @Valid
    @JsonProperty
    private String externalConfigDirectory = "/tmp/openregister_java/external";

    @Valid
    @JsonProperty
    private boolean downloadConfigs = true;

    @SuppressWarnings("unused")
    private FlywayFactory flywayFactory = new FlywayFactory();

    @Valid
    @JsonProperty
    private Map<RegisterName, RegisterContextFactory> registers = new HashMap<>();

    public DataSourceFactory getDatabase() {
        return database;
    }

    public RegisterContextFactory getDefaultRegister() {
        return new RegisterContextFactory(getDatabase(), trackingId, enableRegisterDataDelete, enableDownloadResource, historyPageUrl, custodianName, similarRegisters, credentials);
    }

    public AllTheRegistersFactory getAllTheRegisters() {
        return new AllTheRegistersFactory(getDefaultRegister(), registers, getDefaultRegisterName());
    }

    public RegisterName getDefaultRegisterName() {
        return register;
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
    public String getExternalConfigDirectory() { return externalConfigDirectory; }

    @Override
    public boolean getDownloadConfigs() { return downloadConfigs; }
}
