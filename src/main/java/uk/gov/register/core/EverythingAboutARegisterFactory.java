package uk.gov.register.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import uk.gov.register.configuration.RegistersConfiguration;
import uk.gov.verifiablelog.store.memoization.InMemoryPowOfTwoNoLeaves;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collections;

public class EverythingAboutARegisterFactory {
    @Valid
    @NotNull
    @JsonProperty
    private DataSourceFactory database;

    @SuppressWarnings("unused, used by Jackson")
    public EverythingAboutARegisterFactory() {
    }

    public EverythingAboutARegisterFactory(DataSourceFactory database) {
        this.database = database;
    }

    private FlywayFactory getFlywayFactory(String registerName) {
        FlywayFactory flywayFactory = new FlywayFactory();
        flywayFactory.setLocations(Collections.singletonList("/sql"));
        flywayFactory.setPlaceholders(Collections.singletonMap("registerName", registerName));
        flywayFactory.setOutOfOrder(true);
        return flywayFactory;
    }

    public EverythingAboutARegister build(String registerName, DBIFactory dbiFactory, RegistersConfiguration registersConfiguration, Environment environment) {
        return new EverythingAboutARegister(
                registerName,
                registersConfiguration,
                new InMemoryPowOfTwoNoLeaves(),
                dbiFactory.build(environment, database, registerName),
                getFlywayFactory(registerName).build(database.build(environment.metrics(), registerName + "_flyway")));
    }
}
