package uk.gov.register.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import uk.gov.register.configuration.FieldsConfiguration;
import uk.gov.register.configuration.RegistersConfiguration;
import uk.gov.verifiablelog.store.memoization.InMemoryPowOfTwoNoLeaves;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collections;

public class RegisterContextFactory {
    @Valid
    @NotNull
    @JsonProperty
    private DataSourceFactory database;

    @SuppressWarnings("unused, used by Jackson")
    public RegisterContextFactory() {
    }

    public RegisterContextFactory(DataSourceFactory database) {
        this.database = database;
    }

    private FlywayFactory getFlywayFactory(RegisterName registerName) {
        FlywayFactory flywayFactory = new FlywayFactory();
        flywayFactory.setLocations(Collections.singletonList("/sql"));
        flywayFactory.setPlaceholders(Collections.singletonMap("registerName", registerName.value()));
        flywayFactory.setOutOfOrder(true);
        return flywayFactory;
    }

    public RegisterContext build(RegisterName registerName, DBIFactory dbiFactory, RegistersConfiguration registersConfiguration, FieldsConfiguration fieldsConfiguration, Environment environment) {
        database.getProperties().put("ApplicationName", "openregister");
        return new RegisterContext(
                registerName,
                registersConfiguration,
                fieldsConfiguration,
                new InMemoryPowOfTwoNoLeaves(),
                dbiFactory.build(environment, database, registerName.value()),
                getFlywayFactory(registerName).build(database.build(environment.metrics(), registerName + "_flyway")));
    }
}
