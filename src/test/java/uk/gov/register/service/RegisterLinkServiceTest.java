package uk.gov.register.service;

import org.junit.Test;
import uk.gov.register.configuration.ConfigManager;
import uk.gov.register.configuration.FieldsConfiguration;
import uk.gov.register.configuration.RegistersConfiguration;
import uk.gov.register.core.Cardinality;
import uk.gov.register.core.Field;
import uk.gov.register.core.RegisterId;
import uk.gov.register.core.RegisterMetadata;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegisterLinkServiceTest {
    @Test
    public void getRegistersLinkedFrom_shouldReturnNoRegisters_whenNoOtherRegistersLinkToCurrentRegister() {
        RegisterId localAuthorityRegisterId = new RegisterId("local-authority");
        RegisterMetadata localAuthorityMetadata = new RegisterMetadata(localAuthorityRegisterId, Arrays.asList("local-authority", "name", "address"), "", "", "", "");

        RegistersConfiguration registersConfiguration = mock(RegistersConfiguration.class);
        when(registersConfiguration.getRegisterMetadata(localAuthorityRegisterId)).thenReturn(localAuthorityMetadata);
        when(registersConfiguration.getAllRegisterMetaData()).thenReturn(Arrays.asList(localAuthorityMetadata));

        FieldsConfiguration fieldsConfiguration = mock(FieldsConfiguration.class);
        when(fieldsConfiguration.getAllFields()).thenReturn(Arrays.asList(
                new Field("address", "string", new RegisterId("address"), Cardinality.ONE, ""),
                new Field("local-authority", "string", localAuthorityRegisterId, Cardinality.ONE, ""),
                new Field("name", "string", null, Cardinality.ONE, "")
        ));

        ConfigManager configManager = mock(ConfigManager.class);
        when(configManager.getRegistersConfiguration()).thenReturn(registersConfiguration);
        when(configManager.getFieldsConfiguration()).thenReturn(fieldsConfiguration);

        RegisterLinkService registerLinkService = new RegisterLinkService(configManager);
        List<String> registers = registerLinkService.getRegisterLinks(localAuthorityRegisterId).getRegistersLinkedFrom();

        assertThat(registers, is(empty()));
    }

    @Test
    public void getRegistersLinkedFrom_shouldReturnRegisters_whenOtherRegistersLinkToCurrentRegister() {
        RegisterId addressRegisterId = new RegisterId("address");
        RegisterId companyRegisterId = new RegisterId("company");
        RegisterId localAuthorityRegisterId = new RegisterId("local-authority");
        RegisterMetadata addressMetadata = new RegisterMetadata(addressRegisterId, Arrays.asList("address", "street", "country"), "", "", "", "");
        RegisterMetadata companyMetadata = new RegisterMetadata(companyRegisterId, Arrays.asList("company", "name", "registered-office"), "", "", "", "");
        RegisterMetadata localAuthorityMetadata = new RegisterMetadata(localAuthorityRegisterId, Arrays.asList("local-authority", "name", "address"), "", "", "", "");

        RegistersConfiguration registersConfiguration = mock(RegistersConfiguration.class);
        when(registersConfiguration.getRegisterMetadata(addressRegisterId)).thenReturn(addressMetadata);
        when(registersConfiguration.getRegisterMetadata(companyRegisterId)).thenReturn(companyMetadata);
        when(registersConfiguration.getRegisterMetadata(localAuthorityRegisterId)).thenReturn(localAuthorityMetadata);
        when(registersConfiguration.getAllRegisterMetaData()).thenReturn(Arrays.asList(addressMetadata, companyMetadata, localAuthorityMetadata));

        FieldsConfiguration fieldsConfiguration = mock(FieldsConfiguration.class);
        when(fieldsConfiguration.getAllFields()).thenReturn(Arrays.asList(
                new Field("address", "string", addressRegisterId, Cardinality.ONE, ""),
                new Field("company", "string", companyRegisterId, Cardinality.ONE, ""),
                new Field("local-authority", "string", localAuthorityRegisterId, Cardinality.ONE, ""),
                new Field("name", "string", null, Cardinality.ONE, ""),
                new Field("street", "string", null, Cardinality.ONE, ""),
                new Field("registered-office", "string", addressRegisterId, Cardinality.ONE, "")
        ));

        ConfigManager configManager = mock(ConfigManager.class);
        when(configManager.getRegistersConfiguration()).thenReturn(registersConfiguration);
        when(configManager.getFieldsConfiguration()).thenReturn(fieldsConfiguration);

        RegisterLinkService registerLinkService = new RegisterLinkService(configManager);
        List<String> registers = registerLinkService.getRegisterLinks(addressRegisterId).getRegistersLinkedFrom();

        assertThat(registers, contains("company", "local-authority"));
    }

    @Test
    public void getRegistersLinkedTo_shouldReturnNoRegisters_whenCurrentRegisterLinksToNoOtherRegisters() {
        RegisterId registerId = new RegisterId("address");
        RegisterMetadata registerMetadata = new RegisterMetadata(registerId, Arrays.asList("address", "property", "street", "postcode", "country"), "", "", "", "");

        RegistersConfiguration registersConfiguration = mock(RegistersConfiguration.class);
        when(registersConfiguration.getRegisterMetadata(registerId)).thenReturn(registerMetadata);
        when(registersConfiguration.getAllRegisterMetaData()).thenReturn(Arrays.asList(registerMetadata));

        FieldsConfiguration fieldsConfiguration = mock(FieldsConfiguration.class);
        when(fieldsConfiguration.getAllFields()).thenReturn(Arrays.asList(
                new Field("address", "string", registerId, Cardinality.ONE, ""),
                new Field("property", "string", null, Cardinality.ONE, ""),
                new Field("street", "string", null, Cardinality.ONE, "")
        ));

        ConfigManager configManager = mock(ConfigManager.class);
        when(configManager.getRegistersConfiguration()).thenReturn(registersConfiguration);
        when(configManager.getFieldsConfiguration()).thenReturn(fieldsConfiguration);

        RegisterLinkService registerLinkService = new RegisterLinkService(configManager);
        List<String> registers = registerLinkService.getRegisterLinks(registerId).getRegistersLinkedTo();

        assertThat(registers, is(empty()));
    }

    @Test
    public void getRegistersLinkedTo_shouldReturnRegisters_whenCurrentRegisterLinksToOtherRegisters() {
        RegisterId registerId = new RegisterId("address");
        RegisterMetadata registerMetadata = new RegisterMetadata(registerId, Arrays.asList("address", "property", "street", "postcode", "country", "registry"), "", "", "", "");

        RegistersConfiguration registersConfiguration = mock(RegistersConfiguration.class);
        when(registersConfiguration.getRegisterMetadata(registerId)).thenReturn(registerMetadata);
        when(registersConfiguration.getAllRegisterMetaData()).thenReturn(Arrays.asList(registerMetadata));

        FieldsConfiguration fieldsConfiguration = mock(FieldsConfiguration.class);
        when(fieldsConfiguration.getAllFields()).thenReturn(Arrays.asList(
                new Field("address", "string", registerId, Cardinality.ONE, ""),
                new Field("property", "string", null, Cardinality.ONE, ""),
                new Field("street", "string", null, Cardinality.ONE, ""),
                new Field("postcode", "string", new RegisterId("postcode"), Cardinality.ONE, ""),
                new Field("country", "string", new RegisterId("country"), Cardinality.ONE, ""),
                new Field("registry", "string", new RegisterId("public-body"), Cardinality.ONE, "")
        ));

        ConfigManager configManager = mock(ConfigManager.class);
        when(configManager.getRegistersConfiguration()).thenReturn(registersConfiguration);
        when(configManager.getFieldsConfiguration()).thenReturn(fieldsConfiguration);

        RegisterLinkService registerLinkService = new RegisterLinkService(configManager);
        List<String> registers = registerLinkService.getRegisterLinks(registerId).getRegistersLinkedTo();

        assertThat(registers, contains("postcode", "country", "public-body"));
    }
}
