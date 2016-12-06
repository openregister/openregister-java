package uk.gov.register.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.configuration.RegisterFieldsConfiguration;
import uk.gov.register.db.InMemoryEntryDAO;
import uk.gov.register.exceptions.NoSuchFieldException;
import uk.gov.register.exceptions.NoSuchItemForEntryException;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;
import static uk.gov.register.db.InMemoryStubs.inMemoryEntryLog;
import static uk.gov.register.db.InMemoryStubs.inMemoryItemStore;

public class PostgresRegisterTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final InMemoryEntryDAO entryDAO = new InMemoryEntryDAO(new ArrayList<>());
    private RecordIndex recordIndex;
    private ItemValidator itemValidator;
    private RegisterFieldsConfiguration registerFieldsConfiguration;

    @Before
    public void setup() {
        recordIndex = mock(RecordIndex.class);
        itemValidator = mock(ItemValidator.class);
        registerFieldsConfiguration = mock(RegisterFieldsConfiguration.class);
    }

    @Test(expected = NoSuchFieldException.class)
    public void findMax100RecordsByKeyValueShouldFailWhenKeyDoesNotExist() {
        PostgresRegister register = new PostgresRegister(registerData("register"), registerFieldsConfiguration, inMemoryEntryLog(entryDAO), inMemoryItemStore(itemValidator, entryDAO), recordIndex);
        register.max100RecordsFacetedByKeyValue("citizen-name", "British");
    }

    @Test
    public void findMax100RecordsByKeyValueShouldReturnValueWhenKeyExists() {
        when(registerFieldsConfiguration.containsField("name")).thenReturn(true);
        PostgresRegister register = new PostgresRegister(registerData("register"), registerFieldsConfiguration, inMemoryEntryLog(entryDAO), inMemoryItemStore(itemValidator, entryDAO), recordIndex);
        register.max100RecordsFacetedByKeyValue("name", "United Kingdom");
        verify(recordIndex, times(1)).findMax100RecordsByKeyValue("name", "United Kingdom");
    }

    @Test(expected = NoSuchItemForEntryException.class)
    public void appendEntryShouldThrowExceptionIfNoCorrespondingItemExists() {
        Entry entryDangling = new Entry(106, new HashValue(HashingAlgorithm.SHA256, "item-hash-2"), Instant.now(), "key");

        PostgresRegister register = new PostgresRegister(registerData("register"), registerFieldsConfiguration, inMemoryEntryLog(entryDAO), inMemoryItemStore(itemValidator, entryDAO), recordIndex);
        register.appendEntry(entryDangling);
    }

    @Test
    public void appendEntryShouldNotInsertDanglingEntry() throws IOException {
        Item item = new Item(new HashValue(HashingAlgorithm.SHA256, "item-hash-1"), MAPPER.readTree("{\"register\":\"key-1\"}"));
        Entry entryNotDangling = new Entry(105, new HashValue(HashingAlgorithm.SHA256, "item-hash-1"), Instant.now(), "key-1");
        Entry entryDangling = new Entry(106, new HashValue(HashingAlgorithm.SHA256, "item-hash-2"), Instant.now(), "key-2");

        EntryLog entryLog = inMemoryEntryLog(entryDAO);
        PostgresRegister register = new PostgresRegister(registerData("register"), registerFieldsConfiguration, entryLog, inMemoryItemStore(itemValidator, entryDAO), recordIndex);

        try {
            register.putItem(item);
            register.appendEntry(entryNotDangling);
            register.appendEntry(entryDangling);
        } catch (NoSuchItemForEntryException ignored) {
        }

        assertThat(entryLog.getAllEntries(), equalTo(ImmutableList.of(entryNotDangling)));
    }

    @Test
    public void appendEntryShouldNotInsertRecordForDanglingEntry() throws IOException {
        Item item = new Item(new HashValue(HashingAlgorithm.SHA256, "item-hash-1"), MAPPER.readTree("{\"country\":\"key-1\"}"));
        Entry entryNotDangling = new Entry(105, new HashValue(HashingAlgorithm.SHA256, "item-hash-1"), Instant.now(), "key-1");
        Entry entryDangling = new Entry(106, new HashValue(HashingAlgorithm.SHA256, "item-hash-2"), Instant.now(), "key-2");

        RegisterMetadata registerMetadata = mock(RegisterMetadata.class);
        when(registerMetadata.getRegisterName()).thenReturn("country");

        PostgresRegister register = new PostgresRegister(registerData("country"), registerFieldsConfiguration, inMemoryEntryLog(entryDAO), inMemoryItemStore(itemValidator, entryDAO), recordIndex);

        try {
            register.putItem(item);
            register.appendEntry(entryNotDangling);
            register.appendEntry(entryDangling);
        } catch (NoSuchItemForEntryException ignored) {
        }

        verify(recordIndex, times(1)).updateRecordIndex(eq(entryNotDangling.getKey()), eq(entryNotDangling.getEntryNumber()));
    }

    private RegisterData registerData(String registerName) {
        RegisterData mock = mock(RegisterData.class, RETURNS_DEEP_STUBS);
        when(mock.getRegister().getRegisterName()).thenReturn(registerName);
        return mock;
    }
}
