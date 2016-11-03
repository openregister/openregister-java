package uk.gov.register.core;

import org.junit.Before;
import org.junit.Test;
import uk.gov.register.configuration.RegisterFieldsConfiguration;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.exceptions.NoSuchFieldException;
import uk.gov.register.store.BackingStoreDriver;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

public class PostgresRegisterTest {
    private RegisterNameConfiguration registerNameConfiguration;
    private RegisterFieldsConfiguration registerFieldsConfiguration;
    private BackingStoreDriver backingStoreDriver;

    @Before
    public void setup() {
        registerNameConfiguration = mock(RegisterNameConfiguration.class);
        when(registerNameConfiguration.getRegister()).thenReturn("country");
        ArrayList<String> fields = new ArrayList<>();
        fields.add("name");
        registerFieldsConfiguration = mock(RegisterFieldsConfiguration.class);
        when(registerFieldsConfiguration.getFields()).thenReturn(fields);
        backingStoreDriver = mock(BackingStoreDriver.class);
    }

    @Test(expected = NoSuchFieldException.class)
    public void findMax100RecordsByKeyValueShouldFailWhenKeyDoesNotExist() {
        PostgresRegister register = new PostgresRegister(registerNameConfiguration, registerFieldsConfiguration, backingStoreDriver);
        register.max100RecordsFacetedByKeyValue("citizen-name", "British");
    }

    @Test
    public void findMax100RecordsByKeyValueShouldReturnValueWhenKeyExists() {
        PostgresRegister register = new PostgresRegister(registerNameConfiguration, registerFieldsConfiguration, backingStoreDriver);
        register.max100RecordsFacetedByKeyValue("name", "United Kingdom");
        verify(backingStoreDriver, times(1)).findMax100RecordsByKeyValue(registerNameConfiguration.getRegister(), "name", "United Kingdom");
    }
}