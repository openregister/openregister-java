package uk.gov.register.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.Json;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.register.db.DerivationRecordIndex;
import uk.gov.register.db.InMemoryEntryDAO;
import uk.gov.register.exceptions.ItemValidationException;
import uk.gov.register.exceptions.NoSuchFieldException;
import uk.gov.register.exceptions.SerializationFormatValidationException;
import uk.gov.register.indexer.IndexDriver;
import uk.gov.register.indexer.function.IndexFunction;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
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
    private DataAccessLayer dataAccessLayer;
    //@Mock
    //private RegisterFieldsConfiguration registerFieldsConfiguration;
    @Mock
    private IndexFunction indexFunction;
    @Mock
    private Record fieldRecord;
    @Mock
    private Record registerRecord;

    private PostgresRegister register;

    @Before
    public void setup() throws IOException {
        register = new PostgresRegister(new RegisterName("postcode"),
                inMemoryEntryLog(entryDAO, entryDAO), inMemoryItemStore(itemValidator, entryDAO), recordIndex,
                derivationRecordIndex, Arrays.asList(indexFunction), indexDriver, itemValidator);

        when(registerRecord.getItems()).thenReturn(Arrays.asList(getItem("{\"fields\":[\"postcode\"],\"phase\":\"alpha\",\"register\":\"test\",\"registry\":\"cabinet-office\",\"text\":\"Register of postcodes\"}")));

        when(fieldRecord.getItems()).thenReturn(Arrays.asList(getItem("{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"postcode\",\"phase\":\"alpha\",\"register\":\"postcode\",\"text\":\"field description\"}")));

        when(derivationRecordIndex.getRecord("register:postcode", "metadata")).thenReturn(Optional.of(registerRecord));
        when(derivationRecordIndex.getRecord("field:postcode", "metadata")).thenReturn(Optional.of(fieldRecord));
    }

    @Test(expected = NoSuchFieldException.class)
    public void findMax100RecordsByKeyValueShouldFailWhenKeyDoesNotExist() {
        register.max100RecordsFacetedByKeyValue("citizen-name", "British");
    }

    @Test
    public void findMax100RecordsByKeyValueShouldReturnValueWhenKeyExists() {
        register.max100RecordsFacetedByKeyValue("postcode", "AB1 2CD");
        verify(recordIndex, times(1)).findMax100RecordsByKeyValue("postcode", "AB1 2CD");
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
        doThrow(new ItemValidationException("error", content)).when(itemValidator).validateItem(any(JsonNode.class), anyMap(), any(RegisterMetadata.class));
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

        verify(itemValidator, never()).validateItem(any(), anyMap(), any(RegisterMetadata.class));
    }

    @Test
    public void shouldGetRegisterMetadata() {
        RegisterMetadata registerMetadata = register.getRegisterMetadata();
        assertThat(registerMetadata.getPhase(), is("alpha"));
    }

    @Test
    public void shouldGetFields() {
        Map<String, Field> fieldsByName = register.getFieldsByName();
        assertThat(fieldsByName.size(), is(1));
        assertThat(fieldsByName.get("postcode").getText(), is("field description"));
    }

    private Item getItem(String json) throws IOException {
        return new Item(new ObjectMapper().readTree(json));
    }
}
