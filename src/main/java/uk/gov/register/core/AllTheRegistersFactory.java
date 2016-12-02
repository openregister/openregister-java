package uk.gov.register.core;

import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import uk.gov.register.configuration.RegistersConfiguration;

public class AllTheRegistersFactory {
    private EverythingAboutARegisterFactory defaultRegisterFactory;

    public AllTheRegistersFactory(EverythingAboutARegisterFactory defaultRegisterFactory) {
        this.defaultRegisterFactory = defaultRegisterFactory;
    }

    public AllTheRegisters build(DBIFactory dbiFactory, RegistersConfiguration registersConfiguration, Environment environment) {
        return new AllTheRegisters(defaultRegisterFactory.build(dbiFactory, registersConfiguration, environment));
    }
}
