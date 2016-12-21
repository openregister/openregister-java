package uk.gov.register.configuration;

import uk.gov.register.exceptions.NoSuchConfigException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ConfigManager {
    private final Optional<String> registersConfigFileUrl;
    private final String registersConfigFilePath;
    private final Optional<String> fieldsConfigFileUrl;
    private final String fieldsConfigFilePath;
    private final boolean refresh;
    private final String externalConfigDirectory;

    private AtomicReference<RegistersConfiguration> registersConfiguration = new AtomicReference<>();
    private AtomicReference<FieldsConfiguration> fieldsConfiguration = new AtomicReference<>();

    public ConfigManager(RegisterConfigConfiguration registerConfigConfiguration, Optional<String> registersConfigFileUrl, Optional<String> fieldsConfigFileUrl) {
        this.registersConfigFileUrl = registersConfigFileUrl;
        this.fieldsConfigFileUrl = fieldsConfigFileUrl;
        this.refresh = registerConfigConfiguration.getDownloadConfigs();
        this.externalConfigDirectory = registerConfigConfiguration.getExternalConfigDirectory();
        this.registersConfigFilePath = externalConfigDirectory + "/" + "registers.yaml";
        this.fieldsConfigFilePath = externalConfigDirectory + "/" + "fields.yaml";
    }

    public void refreshConfig() throws NoSuchConfigException, IOException {
        if (refresh) {
            try {
                if (registersConfigFileUrl.isPresent()) {
                    Files.copy(new URL(registersConfigFileUrl.get()).openStream(), Paths.get(registersConfigFilePath), StandardCopyOption.REPLACE_EXISTING);
                }
                if (fieldsConfigFileUrl.isPresent()) {
                    Files.copy(new URL(fieldsConfigFileUrl.get()).openStream(), Paths.get(fieldsConfigFilePath), StandardCopyOption.REPLACE_EXISTING);
                }

                registersConfiguration.set(createRegistersConfiguration());
                fieldsConfiguration.set(createFieldsConfiguration());
            } catch (FileNotFoundException e) {
                throw new NoSuchConfigException(e);
            }
        }
    }

    public RegistersConfiguration getRegistersConfiguration() {
        return registersConfiguration.get();
    }

    public FieldsConfiguration getFieldsConfiguration() {
        return fieldsConfiguration.get();
    }

    private RegistersConfiguration createRegistersConfiguration() {
        return new RegistersConfiguration(registersConfigFileUrl.map(s -> registersConfigFilePath));
    }

    private FieldsConfiguration createFieldsConfiguration() {
        return new FieldsConfiguration(fieldsConfigFileUrl.map(s -> fieldsConfigFilePath));
    }
}