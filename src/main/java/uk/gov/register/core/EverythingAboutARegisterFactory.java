package uk.gov.register.core;

import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.flyway.FlywayFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import uk.gov.register.configuration.RegistersConfiguration;
import uk.gov.verifiablelog.store.memoization.InMemoryPowOfTwoNoLeaves;

public class EverythingAboutARegisterFactory {
    private String registerName;
    private PooledDataSourceFactory database;
    private FlywayFactory flywayFactory;

    public EverythingAboutARegisterFactory(String registerName, PooledDataSourceFactory database, FlywayFactory flywayFactory) {
        this.registerName = registerName;
        this.database = database;
        this.flywayFactory = flywayFactory;
    }

    public EverythingAboutARegister build(DBIFactory dbiFactory, RegistersConfiguration registersConfiguration, Environment environment) {
        return new EverythingAboutARegister(
                registerName,
                registersConfiguration,
                new InMemoryPowOfTwoNoLeaves(),
                dbiFactory.build(environment, database, registerName),
                flywayFactory.build(database.build(environment.metrics(), registerName + "_flyway")));
    }
}
