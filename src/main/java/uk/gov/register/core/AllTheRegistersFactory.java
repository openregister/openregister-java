package uk.gov.register.core;

import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import uk.gov.register.configuration.RegistersConfiguration;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class AllTheRegistersFactory {
    private EverythingAboutARegisterFactory defaultRegisterFactory;
    private final Map<String, EverythingAboutARegisterFactory> otherRegisters;
    private final String defaultRegisterName;

    public AllTheRegistersFactory(EverythingAboutARegisterFactory defaultRegisterFactory, Map<String, EverythingAboutARegisterFactory> otherRegisters, String defaultRegisterName) {
        this.defaultRegisterFactory = defaultRegisterFactory;
        this.otherRegisters = otherRegisters;
        this.defaultRegisterName = defaultRegisterName;
    }

    public AllTheRegisters build(DBIFactory dbiFactory, RegistersConfiguration registersConfiguration, Environment environment) {
        Map<String, EverythingAboutARegister> builtRegisters = otherRegisters.entrySet().stream().collect(toMap(Map.Entry::getKey,
                e -> buildRegister(e.getKey(), e.getValue(), dbiFactory, registersConfiguration, environment)));
        return new AllTheRegisters(
                defaultRegisterFactory.build(defaultRegisterName, dbiFactory, registersConfiguration, environment),
                builtRegisters
        );
    }

    public EverythingAboutARegister buildRegister(String registerName, EverythingAboutARegisterFactory registerFactory, DBIFactory dbiFactory, RegistersConfiguration registersConfiguration, Environment environment) {
        return registerFactory.build(registerName, dbiFactory, registersConfiguration, environment);
    }
}
