package uk.gov.register.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.flyway.FlywayFactory;
import uk.gov.register.auth.RegisterAuthenticatorFactory;
import uk.gov.register.configuration.ConfigManager;
import uk.gov.register.configuration.DatabaseManager;
import uk.gov.register.service.RegisterLinkService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

public class RegisterContextFactory {

    @Valid
    @JsonProperty
    private Optional<String> trackingId;

    @Valid
    @JsonProperty
    private boolean enableRegisterDataDelete = false;

    @Valid
    @JsonProperty
    private boolean enableDownloadResource = false;

    @Valid
    @JsonProperty
    private String schema;

    @SuppressWarnings("unused")
    @Valid
    @JsonProperty
    private Optional<String> historyPageUrl = Optional.empty();

    @Valid
    @JsonProperty
    private Optional<String> custodianName = Optional.empty();

    @Valid
    @JsonProperty
    private List<String> similarRegisters = emptyList();

    @Valid
    @NotNull
    @JsonProperty
    private RegisterAuthenticatorFactory credentials;

    @Valid
    @JsonProperty
    private List<String> indexes = emptyList();

    @JsonCreator
    public RegisterContextFactory(
            @JsonProperty("trackingId") Optional<String> trackingId,
            @JsonProperty("enableRegisterDataDelete") boolean enableRegisterDataDelete,
            @JsonProperty("enableDownloadResource") boolean enableDownloadResource,
            @JsonProperty("schema") String schema,
            @JsonProperty("historyPageUrl") Optional<String> historyPageUrl,
            @JsonProperty("custodianName") Optional<String> custodianName,
            @JsonProperty("similarRegisters") List<String> similarRegisters,
            @JsonProperty("indexes") List<String> indexes,
            @JsonProperty("credentials") RegisterAuthenticatorFactory credentials) {
        this.trackingId = trackingId;
        this.enableRegisterDataDelete = enableRegisterDataDelete;
        this.enableDownloadResource = enableDownloadResource;
        this.schema = schema;
        this.historyPageUrl = historyPageUrl;
        this.custodianName = custodianName;
        this.similarRegisters = similarRegisters != null ? similarRegisters : emptyList();
        this.indexes = indexes != null ? indexes : emptyList();
        this.credentials = credentials;
    }

    private FlywayFactory getFlywayFactory(RegisterName registerName) {
        FlywayFactory flywayFactory = new FlywayFactory();
        flywayFactory.setLocations(Collections.singletonList("/sql"));
        flywayFactory.setPlaceholders(Collections.singletonMap("registerName", registerName.value()));
        flywayFactory.setOutOfOrder(true);
        return flywayFactory;
    }

    public RegisterContext build(RegisterName registerName, ConfigManager configManager, DatabaseManager databaseManager, RegisterLinkService registerLinkService) {
        return new RegisterContext(
                registerName,
                configManager,
                registerLinkService,
                databaseManager.getDbi(),
                getFlywayFactory(registerName).build(databaseManager.getDataSource()),
                schema,
                trackingId,
                enableRegisterDataDelete,
                enableDownloadResource,
                historyPageUrl,
                custodianName,
                similarRegisters,
                indexes,
                credentials.buildAuthenticator());
    }
}
