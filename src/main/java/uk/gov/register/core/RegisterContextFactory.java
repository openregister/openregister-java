package uk.gov.register.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.flyway.FlywayFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import uk.gov.register.auth.RegisterAuthenticatorFactory;
import uk.gov.register.configuration.ConfigManager;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RegisterContextFactory {

    @Valid
    @NotNull
    @JsonProperty
    private DataSourceFactory database;

    @Valid
    @JsonProperty
    private Optional<String> trackingId;

    @Valid
    @JsonProperty
    private boolean enableRegisterDataDelete = false;

    @Valid
    @JsonProperty
    private boolean enableDownloadResource = false;

    @SuppressWarnings("unused")
    @Valid
    @JsonProperty
    private Optional<String> historyPageUrl = Optional.empty();

    @Valid
    @JsonProperty
    private Optional<String> custodianName = Optional.empty();

    @Valid
    @JsonProperty
    private Optional<List<String>> similarRegisters = Optional.empty();

    @Valid
    @NotNull
    @JsonProperty
    private RegisterAuthenticatorFactory credentials;

    @JsonCreator
    public RegisterContextFactory(
            @JsonProperty("database") DataSourceFactory database,
            @JsonProperty("trackingId") Optional<String> trackingId,
            @JsonProperty("enableRegisterDataDelete") boolean enableRegisterDataDelete,
            @JsonProperty("enableDownloadResource") boolean enableDownloadResource,
            @JsonProperty("historyPageUrl") Optional<String> historyPageUrl,
            @JsonProperty("custodianName") Optional<String> custodianName,
            @JsonProperty("similarRegisters") Optional<List<String>> similarRegisters,
            @JsonProperty("credentials") RegisterAuthenticatorFactory credentials) {
        this.database = database;
        this.trackingId = trackingId;
        this.enableRegisterDataDelete = enableRegisterDataDelete;
        this.enableDownloadResource = enableDownloadResource;
        this.historyPageUrl = historyPageUrl;
        this.custodianName = custodianName;
        this.similarRegisters = similarRegisters;
        this.credentials = credentials;
    }

    private FlywayFactory getFlywayFactory(RegisterName registerName) {
        FlywayFactory flywayFactory = new FlywayFactory();
        flywayFactory.setLocations(Collections.singletonList("/sql"));
        flywayFactory.setPlaceholders(Collections.singletonMap("registerName", registerName.value()));
        flywayFactory.setOutOfOrder(true);
        return flywayFactory;
    }

    public RegisterContext build(RegisterName registerName, DBIFactory dbiFactory, ConfigManager configManager, Environment environment) {
        database.getProperties().put("ApplicationName", "openregister_" + registerName);
        // dbiFactory.build() will ensure that this dataSource is correctly shut down
        // it will also be shared with flyway
        ManagedDataSource managedDataSource = database.build(environment.metrics(), registerName.value());

        return new RegisterContext(
                registerName,
                configManager,
                dbiFactory.build(environment, database, managedDataSource, registerName.value()),
                getFlywayFactory(registerName).build(managedDataSource),
                trackingId,
                enableRegisterDataDelete,
                enableDownloadResource,
                historyPageUrl,
                custodianName,
                similarRegisters,
                credentials.buildAuthenticator());
    }
}
