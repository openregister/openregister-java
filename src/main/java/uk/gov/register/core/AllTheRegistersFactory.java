package uk.gov.register.core;

import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import uk.gov.register.configuration.ConfigManager;
import uk.gov.register.service.RegisterLinkService;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class AllTheRegistersFactory {
    private RegisterContextFactory defaultRegisterFactory;
    private final Map<RegisterName, RegisterContextFactory> otherRegisters;
    private final RegisterName defaultRegisterName;

    public AllTheRegistersFactory(RegisterContextFactory defaultRegisterFactory, Map<RegisterName, RegisterContextFactory> otherRegisters, RegisterName defaultRegisterName) {
        this.defaultRegisterFactory = defaultRegisterFactory;
        this.otherRegisters = otherRegisters;
        this.defaultRegisterName = defaultRegisterName;
    }

    public AllTheRegisters build(DBIFactory dbiFactory, ConfigManager configManager, Environment environment, RegisterLinkService registerLinkService) {
        Map<RegisterName, RegisterContext> builtRegisters = otherRegisters.entrySet().stream().collect(toMap(Map.Entry::getKey,
                e -> buildRegister(e.getKey(), e.getValue(), dbiFactory, configManager, environment, registerLinkService)));
        return new AllTheRegisters(
                defaultRegisterFactory.build(defaultRegisterName, dbiFactory, configManager, environment, registerLinkService),
                builtRegisters
        );
    }

    private RegisterContext buildRegister(RegisterName registerName, RegisterContextFactory registerFactory, DBIFactory dbiFactory, ConfigManager configManager, Environment environment, RegisterLinkService registerLinkService) {
        return registerFactory.build(registerName, dbiFactory, configManager, environment, registerLinkService);
    }
}
