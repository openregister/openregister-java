package uk.gov.register.configuration;

import uk.gov.register.exceptions.NoSuchConfigException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class ConfigManager {
    private final Optional<String> registersConfigFileUrl;
    private final String registersConfigFilePath;
    private final Optional<String> fieldsConfigFileUrl;
    private final String fieldsConfigFilePath;
    private final boolean refresh;
    private final String externalConfigDirectory;

    private RegistersConfiguration registersConfiguration;
    private FieldsConfiguration fieldsConfiguration;

    public ConfigManager(RegisterConfigConfiguration registerConfigConfiguration, Optional<String> registersConfigFileUrl, Optional<String> fieldsConfigFileUrl) {
        this.registersConfigFileUrl = registersConfigFileUrl;
        this.fieldsConfigFileUrl = fieldsConfigFileUrl;
        this.refresh = registerConfigConfiguration.getDownloadConfigs();
        this.externalConfigDirectory = registerConfigConfiguration.getExternalConfigDirectory();
        this.registersConfigFilePath = externalConfigDirectory + "/" + "registers.yaml";
        this.fieldsConfigFilePath = externalConfigDirectory + "/" + "fields.yaml";
    }

    public synchronized void refreshConfig() throws NoSuchConfigException, IOException {
        if (refresh) {
            try {
                if (registersConfigFileUrl.isPresent()) {
                    Files.copy(new URL(registersConfigFileUrl.get()).openStream(), Paths.get(registersConfigFilePath), StandardCopyOption.REPLACE_EXISTING);
                }
                if (fieldsConfigFileUrl.isPresent()) {
                    Files.copy(new URL(fieldsConfigFileUrl.get()).openStream(), Paths.get(fieldsConfigFilePath), StandardCopyOption.REPLACE_EXISTING);
                }

                registersConfiguration = createRegistersConfiguration();
                fieldsConfiguration = createFieldsConfiguration();
            } catch (FileNotFoundException e) {
                throw new NoSuchConfigException(e);
            }
        }
    }

    public synchronized RegistersConfiguration getRegistersConfiguration() {
        return registersConfiguration;
    }

    public synchronized FieldsConfiguration getFieldsConfiguration() {
        return fieldsConfiguration;
    }

    private RegistersConfiguration createRegistersConfiguration() {
        return new RegistersConfiguration(registersConfigFileUrl.map(s -> registersConfigFilePath));
    }

    private FieldsConfiguration createFieldsConfiguration() {
        return new FieldsConfiguration(fieldsConfigFileUrl.map(s -> fieldsConfigFilePath));
    }
}