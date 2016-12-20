package uk.gov.register.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.gov.register.core.Field;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.exceptions.NoSuchConfigException;
import uk.gov.register.util.ResourceYamlFileReader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigManagerTest {
    @Rule
    public TemporaryFolder externalConfigsFolder = new TemporaryFolder();

    @Test
    public void refreshConfig_shouldNotRefresh_whenRefreshIsDisabled() throws NoSuchConfigException, IOException {
        RegisterConfigConfiguration registerConfigConfiguration = mock(RegisterConfigConfiguration.class);
        when(registerConfigConfiguration.getDownloadConfigs()).thenReturn(false);

        Optional<String> registersConfigFileUrl = Optional.of("file:///config-that-does-not-exist.yaml");
        Optional<String> fieldsConfigFileUrl = Optional.of("file:///config-that-does-not-exist.yaml");

        ConfigManager configManager = new ConfigManager(registerConfigConfiguration, registersConfigFileUrl, fieldsConfigFileUrl);
        configManager.refreshConfig();

        assertNull(configManager.getRegistersConfiguration());
        assertNull(configManager.getFieldsConfiguration());
    }

    @Test
    public void refreshConfig_shouldDownloadAndStoreRegistersConfigFile_whenRegistersConfigFileUrlIsSpecified() throws Exception {
        File createdRegistersFile = externalConfigsFolder.newFile("registers.yaml");
        String externalRegistersUrl = Paths.get("src/test/resources/config/external-registers.yaml").toUri().toString();

        RegisterConfigConfiguration registerConfigConfiguration = mock(RegisterConfigConfiguration.class);
        when(registerConfigConfiguration.getDownloadConfigs()).thenReturn(true);
        when(registerConfigConfiguration.getExternalConfigDirectory()).thenReturn(externalConfigsFolder.getRoot().getAbsolutePath());

        ConfigManager configManager = new ConfigManager(registerConfigConfiguration, Optional.of(externalRegistersUrl), Optional.empty());
        configManager.refreshConfig();
        String expected = new String(Files.readAllBytes(Paths.get("src/test/resources/config/external-registers.yaml")));
        String copiedConfig = new String(Files.readAllBytes(createdRegistersFile.toPath()));
        assertThat(copiedConfig, is(expected));
    }

    @Test
    public void refreshConfig_shouldDownloadAndStoreFieldsConfigFile_whenFieldsConfigFileUrlIsSpecified() throws Exception {
        File createdFieldsFile = externalConfigsFolder.newFile("fields.yaml");
        String externalFieldsUrl = Paths.get("src/test/resources/config/external-fields.yaml").toUri().toString();

        RegisterConfigConfiguration registerConfigConfiguration = mock(RegisterConfigConfiguration.class);
        when(registerConfigConfiguration.getDownloadConfigs()).thenReturn(true);
        when(registerConfigConfiguration.getExternalConfigDirectory()).thenReturn(externalConfigsFolder.getRoot().getAbsolutePath());

        ConfigManager configManager = new ConfigManager(registerConfigConfiguration, Optional.empty(), Optional.of(externalFieldsUrl));
        configManager.refreshConfig();
        String expected = new String(Files.readAllBytes(Paths.get("src/test/resources/config/external-fields.yaml")));
        String copiedConfig = new String(Files.readAllBytes(createdFieldsFile.toPath()));
        assertThat(copiedConfig, is(expected));
    }

    @Test
    public void refreshConfig_shouldRefreshConfigsFromDefaultFiles_whenNoConfigFileUrlsSpecified() throws NoSuchConfigException, IOException {
        RegisterConfigConfiguration registerConfigConfiguration = mock(RegisterConfigConfiguration.class);
        when(registerConfigConfiguration.getDownloadConfigs()).thenReturn(true);

        Optional<String> registersConfigFileUrl = Optional.empty();
        Optional<String> fieldsConfigFileUrl = Optional.empty();

        ResourceYamlFileReader resourceYamlFileReader = new ResourceYamlFileReader();
        Collection<Field> expectedFields = resourceYamlFileReader.readResource(fieldsConfigFileUrl, "config/fields.yaml", new TypeReference<Map<String, Field>>(){});
        Collection<RegisterMetadata> expectedRegisters = resourceYamlFileReader.readResource(registersConfigFileUrl, "config/registers.yaml", new TypeReference<Map<String, RegisterMetadata>>(){});

        ConfigManager configManager = new ConfigManager(registerConfigConfiguration, registersConfigFileUrl, fieldsConfigFileUrl);
        configManager.refreshConfig();

        Collection<Field> actualFields = configManager.getFieldsConfiguration().getAllFields();
        Collection<RegisterMetadata> actualRegisters = configManager.getRegistersConfiguration().getAllRegisterMetaData();

        assertTrue(expectedFields.containsAll(actualFields));
        assertTrue(expectedRegisters.containsAll(actualRegisters));
    }

    @Test
    public void refreshConfig_shouldRefreshFieldsConfigurationUsingFileUrl_whenFieldsConfigFileUrlIsSpecified() throws NoSuchConfigException, IOException {
        String externalConfigsFolderPath = externalConfigsFolder.getRoot().toString();

        RegisterConfigConfiguration registerConfigConfiguration = mock(RegisterConfigConfiguration.class);
        when(registerConfigConfiguration.getDownloadConfigs()).thenReturn(true);
        when(registerConfigConfiguration.getExternalConfigDirectory()).thenReturn(externalConfigsFolderPath);

        Optional<String> registersConfigFileUrl = Optional.empty();
        Optional<String> fieldsConfigFileUrl = Optional.of(Paths.get("src/test/resources/config/external-fields.yaml").toUri().toString());

        ResourceYamlFileReader resourceYamlFileReader = new ResourceYamlFileReader();
        Collection<Field> expectedFields = resourceYamlFileReader.readResource(Optional.of("src/test/resources/config/external-fields.yaml"), "config/fields.yaml", new TypeReference<Map<String, Field>>(){});
        Collection<RegisterMetadata> expectedRegisters = resourceYamlFileReader.readResource(Optional.empty(), "config/registers.yaml", new TypeReference<Map<String, RegisterMetadata>>(){});

        ConfigManager configManager = new ConfigManager(registerConfigConfiguration, registersConfigFileUrl, fieldsConfigFileUrl);
        configManager.refreshConfig();

        Collection<Field> actualFields = configManager.getFieldsConfiguration().getAllFields();
        Collection<RegisterMetadata> actualRegisters = configManager.getRegistersConfiguration().getAllRegisterMetaData();

        assertTrue(expectedFields.containsAll(actualFields));
        assertTrue(expectedRegisters.containsAll(actualRegisters));
    }

    @Test
    public void refreshConfig_shouldRefreshRegistersConfigurationUsingFileUrl_whenRegistersConfigFileUrlIsSpecified() throws NoSuchConfigException, IOException {
        String externalConfigsFolderPath = externalConfigsFolder.getRoot().toString();

        RegisterConfigConfiguration registerConfigConfiguration = mock(RegisterConfigConfiguration.class);
        when(registerConfigConfiguration.getDownloadConfigs()).thenReturn(true);
        when(registerConfigConfiguration.getExternalConfigDirectory()).thenReturn(externalConfigsFolderPath);

        Optional<String> registersConfigFileUrl = Optional.of(Paths.get("src/test/resources/config/external-registers.yaml").toUri().toString());
        Optional<String> fieldsConfigFileUrl = Optional.empty();

        ResourceYamlFileReader resourceYamlFileReader = new ResourceYamlFileReader();
        Collection<Field> expectedFields = resourceYamlFileReader.readResource(Optional.empty(), "config/fields.yaml", new TypeReference<Map<String, Field>>(){});
        Collection<RegisterMetadata> expectedRegisters = resourceYamlFileReader.readResource(Optional.of("src/test/resources/config/external-registers.yaml"), "config/registers.yaml", new TypeReference<Map<String, RegisterMetadata>>(){});

        ConfigManager configManager = new ConfigManager(registerConfigConfiguration, registersConfigFileUrl, fieldsConfigFileUrl);
        configManager.refreshConfig();

        Collection<Field> actualFields = configManager.getFieldsConfiguration().getAllFields();
        Collection<RegisterMetadata> actualRegisters = configManager.getRegistersConfiguration().getAllRegisterMetaData();

        assertTrue(expectedFields.containsAll(actualFields));
        assertTrue(expectedRegisters.containsAll(actualRegisters));
    }

    @Test(expected = NoSuchConfigException.class)
    public void refreshConfig_shouldThrowNoSuchConfigException_whenSpecifiedRegistersConfigFileUrlIsNotFound() throws NoSuchConfigException, IOException {
        RegisterConfigConfiguration registerConfigConfiguration = mock(RegisterConfigConfiguration.class);
        when(registerConfigConfiguration.getDownloadConfigs()).thenReturn(true);

        Optional<String> registersConfigFileUrl = Optional.of("file:///config-that-does-not-exist.yaml");
        Optional<String> fieldsConfigFileUrl = Optional.empty();

        ConfigManager configManager = new ConfigManager(registerConfigConfiguration, registersConfigFileUrl, fieldsConfigFileUrl);
        configManager.refreshConfig();
    }

    @Test(expected = NoSuchConfigException.class)
    public void refreshConfig_shouldThrowNoSuchConfigException_whenSpecifiedFieldsConfigFileUrlIsNotFound() throws NoSuchConfigException, IOException {
        RegisterConfigConfiguration registerConfigConfiguration = mock(RegisterConfigConfiguration.class);
        when(registerConfigConfiguration.getDownloadConfigs()).thenReturn(true);

        Optional<String> registersConfigFileUrl = Optional.empty();
        Optional<String> fieldsConfigFileUrl = Optional.of("file:///config-that-does-not-exist.yaml");

        ConfigManager configManager = new ConfigManager(registerConfigConfiguration, registersConfigFileUrl, fieldsConfigFileUrl);
        configManager.refreshConfig();
    }

    @Test(expected = MalformedURLException.class)
    public void refreshConfig_shouldThrowIOException_whenSpecifiedRegistersConfigUrlIsMalformed() throws IOException, NoSuchConfigException {
        RegisterConfigConfiguration registerConfigConfiguration = mock(RegisterConfigConfiguration.class);
        when(registerConfigConfiguration.getDownloadConfigs()).thenReturn(true);

        Optional<String> registersConfigFileUrl = Optional.of("config-that-does-not-exist.yaml");
        Optional<String> fieldsConfigFileUrl = Optional.empty();

        ConfigManager configManager = new ConfigManager(registerConfigConfiguration, registersConfigFileUrl, fieldsConfigFileUrl);
        configManager.refreshConfig();
    }

    @Test(expected = MalformedURLException.class)
    public void refreshConfig_shouldThrowIOException_whenSpecifiedFieldsConfigUrlIsMalformed() throws IOException, NoSuchConfigException {
        RegisterConfigConfiguration registerConfigConfiguration = mock(RegisterConfigConfiguration.class);
        when(registerConfigConfiguration.getDownloadConfigs()).thenReturn(true);

        Optional<String> registersConfigFileUrl = Optional.empty();
        Optional<String> fieldsConfigFileUrl = Optional.of("config-that-does-not-exist.yaml");

        ConfigManager configManager = new ConfigManager(registerConfigConfiguration, registersConfigFileUrl, fieldsConfigFileUrl);
        configManager.refreshConfig();
    }
}