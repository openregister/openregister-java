package uk.gov.register.service;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.configuration.ConfigManager;
import uk.gov.register.configuration.FieldsConfiguration;
import uk.gov.register.configuration.RegistersConfiguration;
import uk.gov.register.core.*;
import uk.gov.register.exceptions.FieldValidationException;
import uk.gov.register.exceptions.RegisterValidationException;
import uk.gov.register.configuration.IndexFunctionConfiguration.IndexNames;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

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
		when(environmentRegisterMetadata.getRegisterName()).thenReturn(new RegisterName("postcode"));
		when(environmentRegisterMetadata.getPhase()).thenReturn("alpha");
		when(environmentRegisterMetadata.getCopyright()).thenReturn("Contains National Statistics data © [Crown copyright and database right 2013](http://www.nationalarchives.gov.uk/doc/open-government-licence/),\n Contains Ordnance Survey data © [Crown copyright and database right 2013](http://www.ordnancesurvey.co.uk/oswebsite/docs/licences/os-opendata-licence.pdf),\n Contains Royal Mail data © [Royal Mail copyright and database right 2013](http://www.dfpni.gov.uk/lps/index/copyright_licensing_publishing.htm)");
		when(environmentRegisterMetadata.getRegistry()).thenReturn("office-for-national-statistics");
        when(environmentRegisterMetadata.getFields()).thenReturn(Arrays.asList("postcode", "point"));
        when(registersConfiguration.getRegisterMetadata(registerName)).thenReturn(environmentRegisterMetadata);
		when(configManager.getRegistersConfiguration()).thenReturn(registersConfiguration);

		FieldsConfiguration fieldsConfiguration = mock(FieldsConfiguration.class);
		when(fieldsConfiguration.getField("postcode")).thenReturn(Optional.of(new Field("postcode", "string", registerName, Cardinality.ONE, "A postcode")));
		when(fieldsConfiguration.getField("point")).thenReturn(Optional.of(new Field("point", "point", null, Cardinality.ONE, "A geographical location")));
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
		when(localRegisterMetadata.getRegisterName()).thenReturn(new RegisterName("postcode"));
		when(localRegisterMetadata.getPhase()).thenReturn("alpha");
		when(localRegisterMetadata.getCopyright()).thenReturn("Contains National Statistics data © [Crown copyright and database right 2013](http://www.nationalarchives.gov.uk/doc/open-government-licence/),\n Contains Ordnance Survey data © [Crown copyright and database right 2013](http://www.ordnancesurvey.co.uk/oswebsite/docs/licences/os-opendata-licence.pdf),\n Contains Royal Mail data © [Royal Mail copyright and database right 2013](http://www.dfpni.gov.uk/lps/index/copyright_licensing_publishing.htm)");
		when(localRegisterMetadata.getRegistry()).thenReturn("office-for-national-statistics");
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
			assertThat(e.getMessage().equalsIgnoreCase("Field test does not exist in Field Register"), is(true));
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
	
	@Test
	public void validateExistingMetadataAgainstEnvironment_shouldThrowException_whenExistingRegisterDefinitionIsDifferentFromEnvironment() {
		localRegisterMetadata = mock(RegisterMetadata.class);
		when(localRegisterMetadata.getFields()).thenReturn(Arrays.asList("postcode"));
		when(localRegisterMetadata.getRegisterName()).thenReturn(new RegisterName("postcode"));
		
		Register register = mock(Register.class);
		when(register.getTotalDerivationRecords(IndexNames.METADATA)).thenReturn(3);
		when(register.getRegisterMetadata()).thenReturn(localRegisterMetadata);
		
		RegisterContext registerContext = mock(RegisterContext.class);
		when(registerContext.buildOnDemandRegister()).thenReturn(register);
		
		EnvironmentValidator environmentValidator = new EnvironmentValidator(configManager);
		
		try {
			environmentValidator.validateExistingMetadataAgainstEnvironment(registerContext);
			fail();
		} catch (RegisterValidationException e) {
			assertThat(e.getMessage().equalsIgnoreCase("Definition of register postcode does not match Register Register"), equalTo(true));
		}
	}
	
	@Test
	public void validateExistingMetadataAgainstEnvironment_shouldThrowException_whenExistingFieldDefinitionIsDifferentFromEnvironment() {
		localRegisterMetadata = mock(RegisterMetadata.class);
		when(localRegisterMetadata.getFields()).thenReturn(Arrays.asList("postcode", "point"));
		when(localRegisterMetadata.getRegisterName()).thenReturn(new RegisterName("postcode"));

		Field postcodeField = new Field("postcode", "integer", registerName, Cardinality.ONE, "Postcode in the UK");
		Field pointField = new Field("point", "point", null, Cardinality.ONE, "A geographical location");
		
		Register register = mock(Register.class);
		when(register.getTotalDerivationRecords(IndexNames.METADATA)).thenReturn(3);
		when(register.getRegisterMetadata()).thenReturn(localRegisterMetadata);
		when(register.getFieldsByName()).thenReturn(ImmutableMap.of("postcode", postcodeField, "point", pointField));

		RegisterContext registerContext = mock(RegisterContext.class);
		when(registerContext.buildOnDemandRegister()).thenReturn(register);
		
		EnvironmentValidator environmentValidator = new EnvironmentValidator(configManager);
		
		try {
			environmentValidator.validateExistingMetadataAgainstEnvironment(registerContext);
			fail();
		} catch (FieldValidationException e) {
			assertThat(e.getMessage().equalsIgnoreCase("Definition of field postcode does not match Field Register"), equalTo(true));
		}
	}
	
	@Test
	public void validateExistingMetadataAgainstEnvironment_shouldValidateSuccessfully_whenExistingRegisterMetadataMatchesEnvironment() {
		localRegisterMetadata = mock(RegisterMetadata.class);
		when(localRegisterMetadata.getFields()).thenReturn(Arrays.asList("postcode", "point"));
		when(localRegisterMetadata.getRegisterName()).thenReturn(new RegisterName("postcode"));
		when(localRegisterMetadata.getPhase()).thenReturn("alpha");
		when(localRegisterMetadata.getCopyright()).thenReturn("Contains National Statistics data © [Crown copyright and database right 2013](http://www.nationalarchives.gov.uk/doc/open-government-licence/),\n Contains Ordnance Survey data © [Crown copyright and database right 2013](http://www.ordnancesurvey.co.uk/oswebsite/docs/licences/os-opendata-licence.pdf),\n Contains Royal Mail data © [Royal Mail copyright and database right 2013](http://www.dfpni.gov.uk/lps/index/copyright_licensing_publishing.htm)");
		when(localRegisterMetadata.getRegistry()).thenReturn("office-for-national-statistics");

		Field postcodeField = new Field("postcode", "string", registerName, Cardinality.ONE, "Postcode in the UK");
		Field pointField = new Field("point", "point", null, Cardinality.ONE, "A geographical location");

		Register register = mock(Register.class);
		when(register.getTotalDerivationRecords(IndexNames.METADATA)).thenReturn(3);
		when(register.getRegisterMetadata()).thenReturn(localRegisterMetadata);
		when(register.getFieldsByName()).thenReturn(ImmutableMap.of("postcode", postcodeField, "point", pointField));

		RegisterContext registerContext = mock(RegisterContext.class);
		when(registerContext.buildOnDemandRegister()).thenReturn(register);

		EnvironmentValidator environmentValidator = new EnvironmentValidator(configManager);
		environmentValidator.validateExistingMetadataAgainstEnvironment(registerContext);
		
		verify(register).getRegisterMetadata();
	}
	
	@Test
	public void validateExistingMetadataAgainstEnvironment_shouldValidateSuccessfully_whenExistingRegisterIsEmpty() {
		Register register = mock(Register.class);
		when(register.getTotalDerivationRecords(IndexNames.METADATA)).thenReturn(0);

		RegisterContext registerContext = mock(RegisterContext.class);
		when(registerContext.buildOnDemandRegister()).thenReturn(register);

		EnvironmentValidator environmentValidator = new EnvironmentValidator(configManager);
		environmentValidator.validateExistingMetadataAgainstEnvironment(registerContext);

		verify(register, never()).getRegisterMetadata();
	}
}
