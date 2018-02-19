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
import uk.gov.register.service.RegisterLinkService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

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

    @Valid
    @JsonProperty
    private List<String> indexes = emptyList();

    @JsonCreator
    public RegisterContextFactory(
            @JsonProperty("enableRegisterDataDelete") boolean enableRegisterDataDelete,
            @JsonProperty("enableDownloadResource") boolean enableDownloadResource,
            @JsonProperty("schema") String schema,
            @JsonProperty("custodianName") Optional<String> custodianName,
            @JsonProperty("indexes") List<String> indexes,
            @JsonProperty("credentials") RegisterAuthenticatorFactory credentials) {
        this.enableRegisterDataDelete = enableRegisterDataDelete;
        this.enableDownloadResource = enableDownloadResource;
        this.schema = schema;
        this.custodianName = custodianName;
        this.indexes = indexes != null ? indexes : emptyList();
        this.credentials = credentials;
    }

    private FlywayFactory getFlywayFactory(RegisterName registerName, Optional<String> custodianName, RegisterConfigConfiguration registerConfigConfiguration) {
        FlywayFactory flywayFactory = new FlywayFactory();
        flywayFactory.setLocations(Arrays.asList("/sql", "uk.gov.migration"));
        flywayFactory.setPlaceholders(ImmutableMap.of("registerName", registerName.value(), "custodianName", custodianName.orElse(""), "fieldsYamlUrl",registerConfigConfiguration.getFieldsYamlLocation(),
                "registersYamlUrl", registerConfigConfiguration.getRegistersYamlLocation()));
        flywayFactory.setOutOfOrder(true);
        return flywayFactory;
    }

    public RegisterContext build(RegisterName registerName, ConfigManager configManager, DatabaseManager databaseManager,
                                 EnvironmentValidator environmentValidator, RegisterLinkService registerLinkService,
                                 RegisterConfigConfiguration registerConfigConfiguration) {
        return new RegisterContext(
                registerName,
                configManager,
                environmentValidator,
                registerLinkService,
                databaseManager.getDbi(),
                getFlywayFactory(registerName, custodianName, registerConfigConfiguration).build(databaseManager.getDataSource()),
                schema,
                enableRegisterDataDelete,
                enableDownloadResource,
                indexes,
                credentials.buildAuthenticator());
    }
}
