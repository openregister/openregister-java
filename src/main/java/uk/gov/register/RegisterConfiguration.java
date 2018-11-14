package uk.gov.register;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayFactory;
import uk.gov.register.auth.RegisterAuthenticatorFactory;
import uk.gov.register.configuration.DatabaseConfiguration;
import uk.gov.register.configuration.RegisterConfigConfiguration;
import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.core.AllTheRegistersFactory;
import uk.gov.register.core.RegisterContextFactory;
import uk.gov.register.core.RegisterId;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterConfiguration extends Configuration implements RegisterDomainConfiguration,
        RegisterConfigConfiguration,
        DatabaseConfiguration {
    @SuppressWarnings("unused")
    @Valid
    @NotNull
    @JsonProperty
    private DataSourceFactory database;

    @Valid
    @NotNull
    @JsonProperty
    private String schema;

    @Valid
    @NotNull
    @JsonProperty
    private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

    @Valid
    @NotNull
    @JsonProperty
    private String registerDomain = "openregister.org";

    @Valid
    @JsonProperty
    private Optional<String> custodianName = Optional.empty();

    @Valid
    @NotNull
    @JsonProperty
    private RegisterAuthenticatorFactory credentials = new RegisterAuthenticatorFactory();

    @SuppressWarnings("unused")
    @Valid
    @NotNull
    @JsonProperty
    private RegisterId register;

    @Override
    public String getRegisterDomain() {
        return registerDomain;
    }

    @Valid
    @JsonProperty
    private boolean enableDownloadResource = false;

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
    private Map<RegisterId, RegisterContextFactory> registers = new HashMap<>();

    @JsonProperty
    @NotNull
    @Valid
    private String fieldsJsonLocation;

    @JsonProperty
    @NotNull
    @Valid
    private String registersJsonLocation;

    public RegisterContextFactory getDefaultRegister() {
        return new RegisterContextFactory(enableRegisterDataDelete, enableDownloadResource, schema, custodianName, credentials);
    }

    public AllTheRegistersFactory getAllTheRegisters() {
        return new AllTheRegistersFactory(getDefaultRegister(), registers, getDefaultRegisterId());
    }

    public RegisterId getDefaultRegisterId() {
        return register;
    }

    public String getSchema() {
        return schema;
    }

    public DataSourceFactory getDatabase() {
        return database;
    }

    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return jerseyClient;
    }

    @Override
    public String getExternalConfigDirectory() { return externalConfigDirectory; }

    @Override
    public boolean getDownloadConfigs() { return downloadConfigs; }

    @Override
    public String getFieldsJsonLocation() {
        return fieldsJsonLocation;
    }

    @Override
    public String getRegistersJsonLocation() {
        return registersJsonLocation;
    }
}
