package uk.gov.register.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.register.configuration.RegisterFieldsConfiguration;
import uk.gov.register.db.DerivationRecordIndex;
import uk.gov.register.db.InMemoryEntryDAO;
import uk.gov.register.exceptions.ItemValidationException;
import uk.gov.register.exceptions.NoSuchFieldException;
import uk.gov.register.exceptions.SerializationFormatValidationException;
import uk.gov.register.indexer.IndexDriver;
import uk.gov.register.indexer.function.IndexFunction;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static uk.gov.register.db.InMemoryStubs.inMemoryEntryLog;
import static uk.gov.register.db.InMemoryStubs.inMemoryItemStore;

@RunWith(MockitoJUnitRunner.class)
public class PostgresRegisterTest {
    private static ObjectMapper mapper = new ObjectMapper();

    private final InMemoryEntryDAO entryDAO = new InMemoryEntryDAO(new ArrayList<>());

    @Mock
    private RecordIndex recordIndex;
    @Mock
    private DerivationRecordIndex derivationRecordIndex;
    @Mock
    private ItemValidator itemValidator;
    @Mock
    private IndexDriver indexDriver;
    @Mock
    private RegisterFieldsConfiguration registerFieldsConfiguration;
    @Mock
    private IndexFunction indexFunction;

    private PostgresRegister register;

    @Before
    public void setup() {
        register = new PostgresRegister(registerMetadata("register"), registerFieldsConfiguration,
                inMemoryEntryLog(entryDAO, entryDAO), inMemoryItemStore(itemValidator, entryDAO), recordIndex,
                derivationRecordIndex, Arrays.asList(indexFunction), indexDriver, itemValidator);
    }

    @Test(expected = NoSuchFieldException.class)
    public void findMax100RecordsByKeyValueShouldFailWhenKeyDoesNotExist() {
        register.max100RecordsFacetedByKeyValue("citizen-name", "British");
    }

    @Test
    public void findMax100RecordsByKeyValueShouldReturnValueWhenKeyExists() {
        when(registerFieldsConfiguration.containsField("name")).thenReturn(true);
        register.max100RecordsFacetedByKeyValue("name", "United Kingdom");
        verify(recordIndex, times(1)).findMax100RecordsByKeyValue("name", "United Kingdom");
    }

    @Test(expected = SerializationFormatValidationException.class)
    public void shouldFailForUnreferencedItem() {
        Entry entry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "abc"), Instant.now(),
                "key", EntryType.user);

        register.appendEntry(entry);
    }

    @Test(expected = ItemValidationException.class)
    public void shouldFailForInvalidItem() throws IOException {
        JsonNode content = mapper.readTree("{\"foo\":\"bar\"}");
        doThrow(new ItemValidationException("error", content)).when(itemValidator).validateItem(content);
        HashValue hashValue = new HashValue(HashingAlgorithm.SHA256, "abc");
        Item item = new Item(hashValue, content);
        Entry entry = new Entry(1, hashValue, Instant.now(),"key", EntryType.user);

        register.putItem(item);
        register.appendEntry(entry);
    }

    @Test
    public void shouldNotValidateSystemItem() throws IOException {
        JsonNode content = mapper.readTree("{\"foo\":\"bar\"}");
        HashValue hashValue = new HashValue(HashingAlgorithm.SHA256, "abc");
        Item item = new Item(hashValue, content);
        Entry entry = new Entry(1, hashValue, Instant.now(),"key", EntryType.system);

        register.putItem(item);
        register.appendEntry(entry);

        verify(recordIndex).updateRecordIndex(entry);
    }

    private RegisterMetadata registerMetadata(String registerName) {
        RegisterMetadata mock = mock(RegisterMetadata.class);
        when(mock.getRegisterName()).thenReturn(new RegisterName(registerName));
        return mock;
    }
}
