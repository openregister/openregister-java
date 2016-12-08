package uk.gov.register.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.register.configuration.RegisterFieldsConfiguration;
import uk.gov.register.exceptions.NoSuchFieldException;
import uk.gov.register.exceptions.NoSuchItemForEntryException;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.store.BackingStoreDriver;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

public class PostgresRegisterTest {
    private RegisterData registerData;
    private BackingStoreDriver backingStoreDriver;
    private ItemValidator itemValidator;
    private RegisterFieldsConfiguration registerFieldsConfiguration;

    @Before
    public void setup() {
        ArrayList<String> fields = new ArrayList<>();
        fields.add("name");

        RegisterMetadata registerMetadata = mock(RegisterMetadata.class);
        when(registerMetadata.getFields()).thenReturn(fields);

        registerData = mock(RegisterData.class);
        when(registerData.getRegister()).thenReturn(registerMetadata);
        backingStoreDriver = mock(BackingStoreDriver.class);
        itemValidator = mock(ItemValidator.class);
        registerFieldsConfiguration = mock(RegisterFieldsConfiguration.class);
    }

    @Test(expected = NoSuchFieldException.class)
    public void findMax100RecordsByKeyValueShouldFailWhenKeyDoesNotExist() {
        PostgresRegister register = new PostgresRegister(registerData, backingStoreDriver, itemValidator, registerFieldsConfiguration, new InMemoryEntryLog());
        register.max100RecordsFacetedByKeyValue("citizen-name", "British");
    }

    @Test
    public void findMax100RecordsByKeyValueShouldReturnValueWhenKeyExists() {
        when(registerFieldsConfiguration.containsField("name")).thenReturn(true);
        PostgresRegister register = new PostgresRegister(registerData, backingStoreDriver, itemValidator, registerFieldsConfiguration, new InMemoryEntryLog());
        register.max100RecordsFacetedByKeyValue("name", "United Kingdom");
        verify(backingStoreDriver, times(1)).findMax100RecordsByKeyValue("name", "United Kingdom");
    }

    @Test(expected = NoSuchItemForEntryException.class)
    public void appendEntryShouldThrowExceptionIfNoCorrespondingItemExists() {
        Entry entryDangling = new Entry(106, new HashValue(HashingAlgorithm.SHA256, "item-hash-2"), Instant.now(), "key");

        when(backingStoreDriver.getItemBySha256(any(HashValue.class))).thenReturn(Optional.empty());

        PostgresRegister register = new PostgresRegister(registerData, backingStoreDriver, itemValidator, registerFieldsConfiguration, new InMemoryEntryLog());
        register.appendEntry(entryDangling);
    }

    @Test
    public void appendEntryShouldNotInsertDanglingEntry() {
        Item item = new Item(new HashValue(HashingAlgorithm.SHA256, "item-hash-1"), new ObjectMapper().createObjectNode());
        Entry entryNotDangling = new Entry(105, new HashValue(HashingAlgorithm.SHA256, "item-hash-1"), Instant.now(), "key-1");
        Entry entryDangling = new Entry(106, new HashValue(HashingAlgorithm.SHA256, "item-hash-2"), Instant.now(), "key-2");

        when(backingStoreDriver.getItemBySha256(any(HashValue.class)))
                .thenReturn(Optional.of(item))
                .thenReturn(Optional.empty());

        InMemoryEntryLog entryLog = new InMemoryEntryLog();
        PostgresRegister register = new PostgresRegister(registerData, backingStoreDriver, itemValidator, registerFieldsConfiguration, entryLog);

        try {
            register.appendEntry(entryNotDangling);
            register.appendEntry(entryDangling);
        } catch (NoSuchItemForEntryException ex) {
        }

        assertThat(entryLog.getAllEntries(), equalTo(ImmutableList.of(entryNotDangling)));
    }

    @Test
    public void appendEntryShouldNotInsertRecordForDanglingEntry() {
        Item item = new Item(new HashValue(HashingAlgorithm.SHA256, "item-hash-1"), new ObjectMapper().createObjectNode());
        Entry entryNotDangling = new Entry(105, new HashValue(HashingAlgorithm.SHA256, "item-hash-1"), Instant.now(), "key-1");
        Entry entryDangling = new Entry(106, new HashValue(HashingAlgorithm.SHA256, "item-hash-2"), Instant.now(), "key-2");

        when(backingStoreDriver.getItemBySha256(any(HashValue.class)))
                .thenReturn(Optional.of(item))
                .thenReturn(Optional.empty());

        RegisterMetadata registerMetadata = mock(RegisterMetadata.class);
        when(registerMetadata.getRegisterName()).thenReturn("country");
        when(registerData.getRegister()).thenReturn(registerMetadata);

        PostgresRegister register = new PostgresRegister(registerData, backingStoreDriver, itemValidator, registerFieldsConfiguration, new InMemoryEntryLog());

        try {
            register.appendEntry(entryNotDangling);
            register.appendEntry(entryDangling);
        } catch (NoSuchItemForEntryException ex) {
        }

        verify(backingStoreDriver, times(1)).insertRecord(any(), anyString());

        ArgumentCaptor<Record> argumentCaptor = ArgumentCaptor.forClass(Record.class);
        verify(backingStoreDriver, times(1)).insertRecord(argumentCaptor.capture(), eq("country"));
        assertThat(argumentCaptor.getValue().entry, is(entryNotDangling));
        assertThat(argumentCaptor.getValue().item, is(item));
    }
}
