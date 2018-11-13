package uk.gov.register.configuration;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.register.core.Field;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.exceptions.NoSuchConfigException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class ConfigManagerTest {
    @Rule
    public TemporaryFolder externalConfigsFolder = new TemporaryFolder();

    private String missingConfigFileUrl = "file:///config-that-does-not-exist.json";

    private String registersConfigFileUrl = Paths.get("src/test/resources/config/external-registers.json").toUri().toString();

    private String fieldsConfigFileUrl = Paths.get("src/test/resources/config/external-fields.json").toUri().toString();

    private String externalConfigsFolderPath;

    @Mock
    private RegisterConfigConfiguration registerConfigConfiguration;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        externalConfigsFolderPath = externalConfigsFolder.getRoot().toString();
    }

    @Test
    public void refreshConfig_shouldNotRefresh_whenRefreshIsDisabled() throws NoSuchConfigException, IOException {
        when(registerConfigConfiguration.getDownloadConfigs()).thenReturn(false);
        ConfigManager configManager = new ConfigManager(registerConfigConfiguration);
        configManager.refreshConfig();

        assertNull(configManager.getRegistersConfiguration());
        assertNull(configManager.getFieldsConfiguration());
    }

    @Test
    public void refreshConfig_shouldDownloadAndStoreRegistersConfigFile_whenRegistersConfigFileUrlIsSpecified() throws Exception {
        File createdRegistersFile = externalConfigsFolder.newFile("registers.json");

        when(registerConfigConfiguration.getDownloadConfigs()).thenReturn(true);
        when(registerConfigConfiguration.getExternalConfigDirectory()).thenReturn(externalConfigsFolder.getRoot().getAbsolutePath());
        when(registerConfigConfiguration.getFieldsYamlLocation()).thenReturn(this.fieldsConfigFileUrl);
        when(registerConfigConfiguration.getRegistersYamlLocation()).thenReturn(this.registersConfigFileUrl);

        ConfigManager configManager = new ConfigManager(registerConfigConfiguration);
        configManager.refreshConfig();

        String expected = new String(Files.readAllBytes(Paths.get("src/test/resources/config/external-registers.json")));
        String copiedConfig = new String(Files.readAllBytes(createdRegistersFile.toPath()));
        assertThat(copiedConfig, is(expected));
    }

    @Test
    public void refreshConfig_shouldDownloadAndStoreFieldsConfigFile_whenFieldsConfigFileUrlIsSpecified() throws Exception {
        File createdFieldsFile = externalConfigsFolder.newFile("fields.json");

        when(registerConfigConfiguration.getDownloadConfigs()).thenReturn(true);
        when(registerConfigConfiguration.getExternalConfigDirectory()).thenReturn(externalConfigsFolder.getRoot().getAbsolutePath());
        when(registerConfigConfiguration.getFieldsYamlLocation()).thenReturn(this.fieldsConfigFileUrl);
        when(registerConfigConfiguration.getRegistersYamlLocation()).thenReturn(this.registersConfigFileUrl);

        ConfigManager configManager = new ConfigManager(registerConfigConfiguration);
        configManager.refreshConfig();

        String expected = new String(Files.readAllBytes(Paths.get("src/test/resources/config/external-fields.json")));
        String copiedConfig = new String(Files.readAllBytes(createdFieldsFile.toPath()));
        assertThat(copiedConfig, is(expected));
    }


    @Test
    public void refreshConfig_shouldRefreshFieldsConfigurationUsingFileUrl_whenFieldsConfigFileUrlIsSpecified() throws NoSuchConfigException, IOException {

        when(registerConfigConfiguration.getDownloadConfigs()).thenReturn(true);
        when(registerConfigConfiguration.getExternalConfigDirectory()).thenReturn(externalConfigsFolderPath);
        when(registerConfigConfiguration.getRegistersYamlLocation()).thenReturn(registersConfigFileUrl);
        when(registerConfigConfiguration.getFieldsYamlLocation()).thenReturn(fieldsConfigFileUrl);

        ConfigManager configManager = new ConfigManager(registerConfigConfiguration);
        configManager.refreshConfig();

        Collection<Field> actualFields = configManager.getFieldsConfiguration().getAllFields();
        List<String> fieldNames = actualFields.stream().map(f -> f.fieldName).collect(toList());

        assertThat(fieldNames, hasItems("country","food-premises-types","copyright"));
    }

    @Test
    public void refreshConfig_shouldRefreshRegistersConfigurationUsingFileUrl_whenRegistersConfigFileUrlIsSpecified() throws NoSuchConfigException, IOException {

        when(registerConfigConfiguration.getDownloadConfigs()).thenReturn(true);
        when(registerConfigConfiguration.getExternalConfigDirectory()).thenReturn(externalConfigsFolderPath);
        when(registerConfigConfiguration.getRegistersYamlLocation()).thenReturn(registersConfigFileUrl);
        when(registerConfigConfiguration.getFieldsYamlLocation()).thenReturn(fieldsConfigFileUrl);

        ConfigManager configManager = new ConfigManager(registerConfigConfiguration);
        configManager.refreshConfig();

        Collection<RegisterMetadata> allRegisterMetaData = configManager.getRegistersConfiguration().getAllRegisterMetaData();
        List<String> registerIds = allRegisterMetaData.stream().map(md -> md.getRegisterId().toString()).collect(toList());

        assertThat(registerIds, hasItems("food-premises","country"));
    }

    @Test(expected = NoSuchConfigException.class)
    public void refreshConfig_shouldThrowNoSuchConfigException_whenSpecifiedRegistersConfigFileUrlIsNotFound() throws NoSuchConfigException, IOException {
        when(registerConfigConfiguration.getDownloadConfigs()).thenReturn(true);
        when(registerConfigConfiguration.getExternalConfigDirectory()).thenReturn(externalConfigsFolderPath);
        when(registerConfigConfiguration.getFieldsYamlLocation()).thenReturn(this.fieldsConfigFileUrl);
        when(registerConfigConfiguration.getRegistersYamlLocation()).thenReturn(this.missingConfigFileUrl);

        ConfigManager configManager = new ConfigManager(registerConfigConfiguration);
        configManager.refreshConfig();
    }

    @Test(expected = NoSuchConfigException.class)
    public void refreshConfig_shouldThrowNoSuchConfigException_whenSpecifiedFieldsConfigFileUrlIsNotFound() throws NoSuchConfigException, IOException {
        when(registerConfigConfiguration.getDownloadConfigs()).thenReturn(true);
        when(registerConfigConfiguration.getExternalConfigDirectory()).thenReturn(externalConfigsFolderPath);
        when(registerConfigConfiguration.getFieldsYamlLocation()).thenReturn(missingConfigFileUrl);
        when(registerConfigConfiguration.getRegistersYamlLocation()).thenReturn(this.registersConfigFileUrl);

        ConfigManager configManager = new ConfigManager(registerConfigConfiguration);
        configManager.refreshConfig();
    }

    @Test(expected = MalformedURLException.class)
    public void refreshConfig_shouldThrowIOException_whenSpecifiedRegistersConfigUrlIsMalformed() throws IOException, NoSuchConfigException {
        when(registerConfigConfiguration.getDownloadConfigs()).thenReturn(true);
        when(registerConfigConfiguration.getExternalConfigDirectory()).thenReturn(externalConfigsFolderPath);
        when(registerConfigConfiguration.getRegistersYamlLocation()).thenReturn("zzz://config-that-does-not-exist.json");
        when(registerConfigConfiguration.getRegistersYamlLocation()).thenReturn(this.fieldsConfigFileUrl);

        ConfigManager configManager = new ConfigManager(registerConfigConfiguration);
        configManager.refreshConfig();
    }

    @Test(expected = MalformedURLException.class)
    public void refreshConfig_shouldThrowIOException_whenSpecifiedFieldsConfigUrlIsMalformed() throws IOException, NoSuchConfigException {
        when(registerConfigConfiguration.getDownloadConfigs()).thenReturn(true);
        when(registerConfigConfiguration.getExternalConfigDirectory()).thenReturn(externalConfigsFolderPath);
        when(registerConfigConfiguration.getFieldsYamlLocation()).thenReturn("zzz://config-that-does-not-exist.json");
        when(registerConfigConfiguration.getRegistersYamlLocation()).thenReturn(this.registersConfigFileUrl);

        ConfigManager configManager = new ConfigManager(registerConfigConfiguration);
        configManager.refreshConfig();
    }
}
