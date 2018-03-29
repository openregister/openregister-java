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
    private final Map<RegisterId, RegisterContextFactory> otherRegisters;
    private final RegisterId defaultRegisterId;

    public AllTheRegistersFactory(RegisterContextFactory defaultRegisterFactory, Map<RegisterId, RegisterContextFactory> otherRegisters, RegisterId defaultRegisterId) {
        this.defaultRegisterFactory = defaultRegisterFactory;
        this.otherRegisters = otherRegisters;
        this.defaultRegisterId = defaultRegisterId;
    }

    public AllTheRegisters build(ConfigManager configManager, DatabaseManager databaseManager, RegisterLinkService registerLinkService, EnvironmentValidator environmentValidator, RegisterConfigConfiguration registerConfigConfiguration) {
        Map<RegisterId, RegisterContext> builtRegisters = otherRegisters.entrySet().stream().collect(toMap(Map.Entry::getKey,
                e -> buildRegister(e.getKey(), e.getValue(), configManager, databaseManager, environmentValidator, registerLinkService, registerConfigConfiguration)));
        return new AllTheRegisters(
                defaultRegisterFactory.build(defaultRegisterId, configManager, databaseManager, environmentValidator, registerLinkService, registerConfigConfiguration),
                builtRegisters
        );
    }

    private RegisterContext buildRegister(RegisterId registerId,
          RegisterContextFactory registerFactory, 
          ConfigManager configManager, 
          DatabaseManager databaseManager, 
          EnvironmentValidator environmentValidator, 
          RegisterLinkService registerLinkService, 
          RegisterConfigConfiguration registerConfigConfiguration) {
        return registerFactory.build(registerId, configManager, databaseManager, environmentValidator, registerLinkService, registerConfigConfiguration);
    }
}
