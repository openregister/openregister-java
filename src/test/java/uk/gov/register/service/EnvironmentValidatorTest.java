package uk.gov.register.service;

import org.junit.Before;
import org.junit.Test;
import uk.gov.register.configuration.ConfigManager;
import uk.gov.register.configuration.FieldsConfiguration;
import uk.gov.register.configuration.RegistersConfiguration;
import uk.gov.register.core.Cardinality;
import uk.gov.register.core.Field;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.core.RegisterName;
import uk.gov.register.exceptions.FieldValidationException;
import uk.gov.register.exceptions.RegisterValidationException;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EnvironmentValidatorTest {
    private ConfigManager configManager;
    private RegisterMetadata localRegisterMetadata;
    private RegisterMetadata environmentRegisterMetadata;
	private RegisterName registerName = new RegisterName("postcode");

    @Before
    public void setUp() {
        localRegisterMetadata = mock(RegisterMetadata.class);
		environmentRegisterMetadata = mock(RegisterMetadata.class);
		configManager = mock(ConfigManager.class);
        
        RegistersConfiguration registersConfiguration = mock(RegistersConfiguration.class);
        when(localRegisterMetadata.getRegisterName()).thenReturn(registerName);
        when(environmentRegisterMetadata.getFields()).thenReturn(Arrays.asList("postcode", "point"));
        when(registersConfiguration.getRegisterMetadata(registerName)).thenReturn(environmentRegisterMetadata);
		when(configManager.getRegistersConfiguration()).thenReturn(registersConfiguration);

		FieldsConfiguration fieldsConfiguration = mock(FieldsConfiguration.class);
		when(fieldsConfiguration.getField("postcode")).thenReturn(Optional.of(new Field("postcode", "string", registerName, Cardinality.ONE, "A postcode")));
		when(configManager.getFieldsConfiguration()).thenReturn(fieldsConfiguration);
    }

    @Test
    public void validateRegisterDefinitionAgainstEnvironment_shouldThrowException_whenDifferentFieldsAreSpecified() throws Exception {
		when(localRegisterMetadata.getFields()).thenReturn(Arrays.asList("postcode"));
        EnvironmentValidator environmentValidator = new EnvironmentValidator(configManager);
		
		try {
			environmentValidator.validateRegisterAgainstEnvironment(localRegisterMetadata);
			fail();
		} catch (RegisterValidationException e) {
			assertThat(e.getMessage(), equalTo("Definition of register postcode does not match Register Register"));
		}
    }
    
    @Test
    public void validateRegisterDefinitionAgainstEnvironment_shouldNotThrowException_whenSameFieldsAreSpecified() {
		when(localRegisterMetadata.getFields()).thenReturn(Arrays.asList("point", "postcode"));
		EnvironmentValidator environmentValidator = new EnvironmentValidator(configManager);
		environmentValidator.validateRegisterAgainstEnvironment(localRegisterMetadata);
	}
	
	@Test
	public void validateFieldDefinitionsAgainstEnvironment_shouldThrowException_whenFieldIsNotDefinedInEnvironment() {
		EnvironmentValidator environmentValidator = new EnvironmentValidator(configManager);
		Field localField = new Field("test", "string", registerName, Cardinality.ONE, "A test field");

		try {
			environmentValidator.validateFieldAgainstEnvironment(localField);
			fail();
		} catch (FieldValidationException e) {
			assertThat(e.getMessage().equalsIgnoreCase("Field test does not exist in Field Register"), equalTo(true));
		}
	}
	
	@Test
	public void validateFieldDefinitionsAgainstEnvironment_shouldThrowException_whenFieldDefinitionsAreDifferent() {
		EnvironmentValidator environmentValidator = new EnvironmentValidator(configManager);
		Field localField = new Field("postcode", "integer", registerName, Cardinality.ONE, "A postcode");
		
		try {
			environmentValidator.validateFieldAgainstEnvironment(localField);
			fail();
		} catch (FieldValidationException e) {
			assertThat(e.getMessage().equalsIgnoreCase("Definition of field postcode does not match Field Register"), equalTo(true));
		}
	}
	
	@Test
	public void validateFieldDefinitionsAgainstEnvironment_shouldNotThrowException_whenFieldDefinitionsAreEquivalent() {
		EnvironmentValidator environmentValidator = new EnvironmentValidator(configManager);
		Field localField = new Field("postcode", "string", registerName, Cardinality.ONE, "Postcode in the UK");

		environmentValidator.validateFieldAgainstEnvironment(localField);
	}
}
