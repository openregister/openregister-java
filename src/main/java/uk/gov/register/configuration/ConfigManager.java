package uk.gov.register.configuration;

import uk.gov.register.exceptions.NoSuchConfigException;

import java.net.URISyntaxException;
import java.util.Optional;

public interface ConfigManager {
    void tryUpdateConfigs(Optional<String> registersConfigPath, Optional<String> fieldsConfigPath) throws URISyntaxException, NoSuchConfigException;
}