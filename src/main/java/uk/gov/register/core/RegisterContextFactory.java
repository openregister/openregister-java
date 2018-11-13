package uk.gov.register.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.flyway.FlywayFactory;
import uk.gov.register.auth.RegisterAuthenticatorFactory;
import uk.gov.register.configuration.ConfigManager;
import uk.gov.register.configuration.DatabaseManager;
import uk.gov.register.configuration.RegisterConfigConfiguration;
import uk.gov.register.service.EnvironmentValidator;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterContextFactory {

    @Valid
    @JsonProperty
    private boolean enableRegisterDataDelete = false;

    @Valid
    @JsonProperty
    private boolean enableDownloadResource = false;

    @Valid
    @JsonProperty
    private String schema;

    @Valid
    @JsonProperty
    private Optional<String> custodianName = Optional.empty();

    @Valid
    @NotNull
    @JsonProperty
    private RegisterAuthenticatorFactory credentials;

    @JsonCreator
    public RegisterContextFactory(
            @JsonProperty("enableRegisterDataDelete") boolean enableRegisterDataDelete,
            @JsonProperty("enableDownloadResource") boolean enableDownloadResource,
            @JsonProperty("schema") String schema,
            @JsonProperty("custodianName") Optional<String> custodianName,
            @JsonProperty("credentials") RegisterAuthenticatorFactory credentials) {
        this.enableRegisterDataDelete = enableRegisterDataDelete;
        this.enableDownloadResource = enableDownloadResource;
        this.schema = schema;
        this.custodianName = custodianName;
        this.credentials = credentials;
    }

    private FlywayFactory getFlywayFactory(RegisterId registerId, Optional<String> custodianName, RegisterConfigConfiguration registerConfigConfiguration) {
        FlywayFactory flywayFactory = new FlywayFactory();
        flywayFactory.setLocations(Arrays.asList("/sql", "uk.gov.migration"));
        flywayFactory.setPlaceholders(ImmutableMap.of("registerName", registerId.value(), "custodianName", custodianName.orElse(""), "fieldsJsonUrl",registerConfigConfiguration.getFieldsJsonLocation(),
                "registersJsonUrl", registerConfigConfiguration.getRegistersJsonLocation()));
        flywayFactory.setOutOfOrder(true);
        return flywayFactory;
    }

    public RegisterContext build(RegisterId registerId, ConfigManager configManager, DatabaseManager databaseManager,
                                 EnvironmentValidator environmentValidator,
                                 RegisterConfigConfiguration registerConfigConfiguration) {
        return new RegisterContext(
                registerId,
                configManager,
                environmentValidator,
                databaseManager.getDbi(),
                getFlywayFactory(registerId, custodianName, registerConfigConfiguration).build(databaseManager.getDataSource()),
                schema,
                enableRegisterDataDelete,
                enableDownloadResource,
                credentials.buildAuthenticator());
    }
}
