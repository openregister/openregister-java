package uk.gov.register.service;

import org.junit.Test;
import uk.gov.register.configuration.ConfigManager;
import uk.gov.register.configuration.FieldsConfiguration;
import uk.gov.register.configuration.RegistersConfiguration;
import uk.gov.register.core.Cardinality;
import uk.gov.register.core.Field;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.core.RegisterName;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegisterLinkServiceTest {
    @Test
    public void getRegistersLinkedFrom_shouldReturnNoRegisters_whenNoOtherRegistersLinkToCurrentRegister() {
        RegisterName localAuthorityRegisterName = new RegisterName("local-authority");
        RegisterMetadata localAuthorityMetadata = new RegisterMetadata(localAuthorityRegisterName, Arrays.asList("local-authority", "name", "address"), "", "", "", "");

        RegistersConfiguration registersConfiguration = mock(RegistersConfiguration.class);
        when(registersConfiguration.getRegisterMetadata(localAuthorityRegisterName)).thenReturn(localAuthorityMetadata);
        when(registersConfiguration.getAllRegisterMetaData()).thenReturn(Arrays.asList(localAuthorityMetadata));

        FieldsConfiguration fieldsConfiguration = mock(FieldsConfiguration.class);
        when(fieldsConfiguration.getAllFields()).thenReturn(Arrays.asList(
                new Field("address", "string", new RegisterName("address"), Cardinality.ONE, ""),
                new Field("local-authority", "string", localAuthorityRegisterName, Cardinality.ONE, ""),
                new Field("name", "string", null, Cardinality.ONE, "")
        ));

        ConfigManager configManager = mock(ConfigManager.class);
        when(configManager.getRegistersConfiguration()).thenReturn(registersConfiguration);
        when(configManager.getFieldsConfiguration()).thenReturn(fieldsConfiguration);

        RegisterLinkService registerLinkService = new RegisterLinkService(configManager);
        List<String> registers = registerLinkService.getRegisterLinks(localAuthorityRegisterName).getRegistersLinkedFrom();

        assertThat(registers, is(empty()));
    }

    @Test
    public void getRegistersLinkedFrom_shouldReturnRegisters_whenOtherRegistersLinkToCurrentRegister() {
        RegisterName addressRegisterName = new RegisterName("address");
        RegisterName companyRegisterName = new RegisterName("company");
        RegisterName localAuthorityRegisterName = new RegisterName("local-authority");
        RegisterMetadata addressMetadata = new RegisterMetadata(addressRegisterName, Arrays.asList("address", "street", "country"), "", "", "", "");
        RegisterMetadata companyMetadata = new RegisterMetadata(companyRegisterName, Arrays.asList("company", "name", "registered-office"), "", "", "", "");
        RegisterMetadata localAuthorityMetadata = new RegisterMetadata(localAuthorityRegisterName, Arrays.asList("local-authority", "name", "address"), "", "", "", "");

        RegistersConfiguration registersConfiguration = mock(RegistersConfiguration.class);
        when(registersConfiguration.getRegisterMetadata(addressRegisterName)).thenReturn(addressMetadata);
        when(registersConfiguration.getRegisterMetadata(companyRegisterName)).thenReturn(companyMetadata);
        when(registersConfiguration.getRegisterMetadata(localAuthorityRegisterName)).thenReturn(localAuthorityMetadata);
        when(registersConfiguration.getAllRegisterMetaData()).thenReturn(Arrays.asList(addressMetadata, companyMetadata, localAuthorityMetadata));

        FieldsConfiguration fieldsConfiguration = mock(FieldsConfiguration.class);
        when(fieldsConfiguration.getAllFields()).thenReturn(Arrays.asList(
                new Field("address", "string", addressRegisterName, Cardinality.ONE, ""),
                new Field("company", "string", companyRegisterName, Cardinality.ONE, ""),
                new Field("local-authority", "string", localAuthorityRegisterName, Cardinality.ONE, ""),
                new Field("name", "string", null, Cardinality.ONE, ""),
                new Field("street", "string", null, Cardinality.ONE, ""),
                new Field("registered-office", "string", addressRegisterName, Cardinality.ONE, "")
        ));

        ConfigManager configManager = mock(ConfigManager.class);
        when(configManager.getRegistersConfiguration()).thenReturn(registersConfiguration);
        when(configManager.getFieldsConfiguration()).thenReturn(fieldsConfiguration);

        RegisterLinkService registerLinkService = new RegisterLinkService(configManager);
        List<String> registers = registerLinkService.getRegisterLinks(addressRegisterName).getRegistersLinkedFrom();

        assertThat(registers, contains("company", "local-authority"));
    }

    @Test
    public void getRegistersLinkedTo_shouldReturnNoRegisters_whenCurrentRegisterLinksToNoOtherRegisters() {
        RegisterName registerName = new RegisterName("address");
        RegisterMetadata registerMetadata = new RegisterMetadata(registerName, Arrays.asList("address", "property", "street", "postcode", "country"), "", "", "", "");

        RegistersConfiguration registersConfiguration = mock(RegistersConfiguration.class);
        when(registersConfiguration.getRegisterMetadata(registerName)).thenReturn(registerMetadata);
        when(registersConfiguration.getAllRegisterMetaData()).thenReturn(Arrays.asList(registerMetadata));

        FieldsConfiguration fieldsConfiguration = mock(FieldsConfiguration.class);
        when(fieldsConfiguration.getAllFields()).thenReturn(Arrays.asList(
                new Field("address", "string", registerName, Cardinality.ONE, ""),
                new Field("property", "string", null, Cardinality.ONE, ""),
                new Field("street", "string", null, Cardinality.ONE, "")
        ));

        ConfigManager configManager = mock(ConfigManager.class);
        when(configManager.getRegistersConfiguration()).thenReturn(registersConfiguration);
        when(configManager.getFieldsConfiguration()).thenReturn(fieldsConfiguration);

        RegisterLinkService registerLinkService = new RegisterLinkService(configManager);
        List<String> registers = registerLinkService.getRegisterLinks(registerName).getRegistersLinkedTo();

        assertThat(registers, is(empty()));
    }

    @Test
    public void getRegistersLinkedTo_shouldReturnRegisters_whenCurrentRegisterLinksToOtherRegisters() {
        RegisterName registerName = new RegisterName("address");
        RegisterMetadata registerMetadata = new RegisterMetadata(registerName, Arrays.asList("address", "property", "street", "postcode", "country", "registry"), "", "", "", "");

        RegistersConfiguration registersConfiguration = mock(RegistersConfiguration.class);
        when(registersConfiguration.getRegisterMetadata(registerName)).thenReturn(registerMetadata);
        when(registersConfiguration.getAllRegisterMetaData()).thenReturn(Arrays.asList(registerMetadata));

        FieldsConfiguration fieldsConfiguration = mock(FieldsConfiguration.class);
        when(fieldsConfiguration.getAllFields()).thenReturn(Arrays.asList(
                new Field("address", "string", registerName, Cardinality.ONE, ""),
                new Field("property", "string", null, Cardinality.ONE, ""),
                new Field("street", "string", null, Cardinality.ONE, ""),
                new Field("postcode", "string", new RegisterName("postcode"), Cardinality.ONE, ""),
                new Field("country", "string", new RegisterName("country"), Cardinality.ONE, ""),
                new Field("registry", "string", new RegisterName("public-body"), Cardinality.ONE, "")
        ));

        ConfigManager configManager = mock(ConfigManager.class);
        when(configManager.getRegistersConfiguration()).thenReturn(registersConfiguration);
        when(configManager.getFieldsConfiguration()).thenReturn(fieldsConfiguration);

        RegisterLinkService registerLinkService = new RegisterLinkService(configManager);
        List<String> registers = registerLinkService.getRegisterLinks(registerName).getRegistersLinkedTo();

        assertThat(registers, contains("postcode", "country", "public-body"));
    }
}