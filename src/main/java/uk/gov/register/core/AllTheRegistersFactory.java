package uk.gov.register.core;

import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import uk.gov.register.configuration.FieldsConfiguration;
import uk.gov.register.configuration.RegistersConfiguration;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class AllTheRegistersFactory {
    private RegisterContextFactory defaultRegisterFactory;
    private final Map<String, RegisterContextFactory> otherRegisters;
    private final String defaultRegisterName;

    public AllTheRegistersFactory(RegisterContextFactory defaultRegisterFactory, Map<String, RegisterContextFactory> otherRegisters, String defaultRegisterName) {
        this.defaultRegisterFactory = defaultRegisterFactory;
        this.otherRegisters = otherRegisters;
        this.defaultRegisterName = defaultRegisterName;
    }

    public AllTheRegisters build(DBIFactory dbiFactory, RegistersConfiguration registersConfiguration, FieldsConfiguration fieldsConfiguration, Environment environment) {
        Map<String, RegisterContext> builtRegisters = otherRegisters.entrySet().stream().collect(toMap(Map.Entry::getKey,
                e -> buildRegister(e.getKey(), e.getValue(), dbiFactory, registersConfiguration, fieldsConfiguration, environment)));
        return new AllTheRegisters(
                defaultRegisterFactory.build(defaultRegisterName, dbiFactory, registersConfiguration, fieldsConfiguration, environment),
                builtRegisters
        );
    }

    private RegisterContext buildRegister(String registerName, RegisterContextFactory registerFactory, DBIFactory dbiFactory, RegistersConfiguration registersConfiguration, FieldsConfiguration fieldsConfiguration, Environment environment) {
        return registerFactory.build(registerName, dbiFactory, registersConfiguration, fieldsConfiguration, environment);
    }
}
