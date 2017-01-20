package uk.gov.register.configuration;

import uk.gov.register.exceptions.NoSuchConfigException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicReference;

public class ConfigManager {
    private final String registersConfigFileUrl;
    private final String registersConfigFilePath;
    private final String fieldsConfigFileUrl;
    private final String fieldsConfigFilePath;
    private final boolean refresh;

    private AtomicReference<RegistersConfiguration> registersConfiguration = new AtomicReference<>();
    private AtomicReference<FieldsConfiguration> fieldsConfiguration = new AtomicReference<>();

    public ConfigManager(RegisterConfigConfiguration registerConfigConfiguration) {
        this.registersConfigFileUrl = registerConfigConfiguration.getRegistersYamlLocation();
        this.fieldsConfigFileUrl = registerConfigConfiguration.getFieldsYamlLocation();
        this.refresh = registerConfigConfiguration.getDownloadConfigs();

        String externalConfigDirectory = registerConfigConfiguration.getExternalConfigDirectory();
        this.registersConfigFilePath = externalConfigDirectory + "/" + "registers.yaml";
        this.fieldsConfigFilePath = externalConfigDirectory + "/" + "fields.yaml";
    }

    public void refreshConfig() throws NoSuchConfigException, IOException {
        if (refresh) {
            try {
                Files.copy(new URL(registersConfigFileUrl).openStream(), Paths.get(registersConfigFilePath), StandardCopyOption.REPLACE_EXISTING);
                Files.copy(new URL(fieldsConfigFileUrl).openStream(), Paths.get(fieldsConfigFilePath), StandardCopyOption.REPLACE_EXISTING);
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
        return new RegistersConfiguration(registersConfigFilePath);
    }

    private FieldsConfiguration createFieldsConfiguration() {
        return new FieldsConfiguration(fieldsConfigFilePath);
    }
}