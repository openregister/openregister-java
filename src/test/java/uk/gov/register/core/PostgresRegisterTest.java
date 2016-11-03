package uk.gov.register.core;

import org.junit.Before;
import org.junit.Test;
import uk.gov.register.exceptions.NoSuchFieldException;
import uk.gov.register.store.BackingStoreDriver;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

public class PostgresRegisterTest {
    private RegisterData registerData;
    private BackingStoreDriver backingStoreDriver;

    @Before
    public void setup() {
        ArrayList<String> fields = new ArrayList<>();
        fields.add("name");

        RegisterMetadata registerMetadata = mock(RegisterMetadata.class);
        when(registerMetadata.getFields()).thenReturn(fields);

        registerData = mock(RegisterData.class);
        when(registerData.getRegister()).thenReturn(registerMetadata);
        backingStoreDriver = mock(BackingStoreDriver.class);
    }

    @Test(expected = NoSuchFieldException.class)
    public void findMax100RecordsByKeyValueShouldFailWhenKeyDoesNotExist() {
        PostgresRegister register = new PostgresRegister(registerData, backingStoreDriver);
        register.max100RecordsFacetedByKeyValue("citizen-name", "British");
    }

    @Test
    public void findMax100RecordsByKeyValueShouldReturnValueWhenKeyExists() {
        PostgresRegister register = new PostgresRegister(registerData, backingStoreDriver);
        register.max100RecordsFacetedByKeyValue("name", "United Kingdom");
        verify(backingStoreDriver, times(1)).findMax100RecordsByKeyValue("name", "United Kingdom");
    }
}