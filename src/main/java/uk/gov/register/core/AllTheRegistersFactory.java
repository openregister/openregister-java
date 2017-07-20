package uk.gov.register.core;

import uk.gov.register.configuration.ConfigManager;
import uk.gov.register.configuration.DatabaseManager;
import uk.gov.register.configuration.RegisterConfigConfiguration;
import uk.gov.register.service.EnvironmentValidator;
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

    public AllTheRegisters build(ConfigManager configManager, DatabaseManager databaseManager, RegisterLinkService registerLinkService, EnvironmentValidator environmentValidator, RegisterConfigConfiguration registerConfigConfiguration) {
        Map<RegisterName, RegisterContext> builtRegisters = otherRegisters.entrySet().stream().collect(toMap(Map.Entry::getKey,
                e -> buildRegister(e.getKey(), e.getValue(), configManager, databaseManager, environmentValidator, registerLinkService, registerConfigConfiguration)));
        return new AllTheRegisters(
                defaultRegisterFactory.build(defaultRegisterName, configManager, databaseManager, environmentValidator, registerLinkService, registerConfigConfiguration),
                builtRegisters
        );
    }

    private RegisterContext buildRegister(RegisterName registerName, 
          RegisterContextFactory registerFactory, 
          ConfigManager configManager, 
          DatabaseManager databaseManager, 
          EnvironmentValidator environmentValidator, 
          RegisterLinkService registerLinkService, 
          RegisterConfigConfiguration registerConfigConfiguration) {
        return registerFactory.build(registerName, configManager, databaseManager, environmentValidator, registerLinkService, registerConfigConfiguration);
    }
}
