package uk.gov.register.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.register.configuration.IndexFunctionConfiguration.IndexNames;
import uk.gov.register.db.Index;
import uk.gov.register.db.InMemoryEntryDAO;
import uk.gov.register.exceptions.AppendEntryException;
import uk.gov.register.exceptions.NoSuchFieldException;
import uk.gov.register.exceptions.BlobValidationException;
import uk.gov.register.indexer.IndexDriver;
import uk.gov.register.indexer.function.IndexFunction;
import uk.gov.register.service.EnvironmentValidator;
import uk.gov.register.service.BlobValidator;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

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
    private Index index;
    @Mock
    private BlobValidator blobValidator;
    @Mock
    private IndexDriver indexDriver;
    @Mock
    private IndexFunction indexFunction;
    @Mock
    private IndexFunction systemIndexFunction;
    @Mock
    private Record fieldRecord;
    @Mock
    private Record registerRecord;
    @Mock
    private EnvironmentValidator environmentValidator;


    private PostgresRegister register;

    private String postcodeRegisterItem = "{\"fields\":[\"postcode\"],\"phase\":\"alpha\",\"register\":\"r\",\"registry\":\"r\",\"text\":\"t\"}";

    @Before
    public void setup() throws IOException {

        Map<EntryType, Collection<IndexFunction>> indexFunctionsByEntryType = ImmutableMap.of(EntryType.system, Arrays.asList(systemIndexFunction), EntryType.user, Arrays.asList(indexFunction));

        register = new PostgresRegister(new RegisterId("postcode"),
                inMemoryEntryLog(entryDAO, entryDAO), inMemoryItemStore(blobValidator, entryDAO),
                index, indexFunctionsByEntryType, blobValidator, environmentValidator);

        when(registerRecord.getBlobs()).thenReturn(Arrays.asList(getItem(postcodeRegisterItem)));

        when(fieldRecord.getBlobs()).thenReturn(Arrays.asList(getItem("{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"postcode\",\"phase\":\"alpha\",\"register\":\"postcode\",\"text\":\"field description\"}")));

        when(index.getRecord("register:postcode", IndexNames.METADATA)).thenReturn(Optional.of(registerRecord));
        when(index.getRecord("field:postcode", IndexNames.METADATA)).thenReturn(Optional.of(fieldRecord));
    }

    @Test(expected = NoSuchFieldException.class)
    public void findMax100RecordsByKeyValueShouldFailWhenKeyDoesNotExist() {
        register.max100RecordsFacetedByKeyValue("citizen-name", "British");
    }

    @Test
    public void findMax100RecordsByKeyValueShouldReturnValueWhenKeyExists() {
        register.max100RecordsFacetedByKeyValue("postcode", "AB1 2CD");
        verify(index, times(1)).findMax100RecordsByKeyValue("postcode", "AB1 2CD");
    }

    @Test(expected = AppendEntryException.class)
    public void shouldFailForUnreferencedItem() {
        Entry entry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "abc"), Instant.now(),
                "key", EntryType.user);

        register.appendEntry(entry);
    }

    @Test(expected = AppendEntryException.class)
    public void shouldFailForInvalidItem() throws IOException {
        JsonNode content = mapper.readTree("{\"foo\":\"bar\"}");
        doThrow(new BlobValidationException("error", content)).when(blobValidator).validateBlob(any(JsonNode.class), anyMap(), any(RegisterMetadata.class));
        HashValue hashValue = new HashValue(HashingAlgorithm.SHA256, "abc");
        when(index.getRecord("field:postcode", IndexNames.METADATA)).thenReturn(Optional.of(fieldRecord));
        Blob blob = new Blob(hashValue, content);
        Entry entry = new Entry(1, hashValue, Instant.now(),"key", EntryType.user);

        register.addBlob(blob);
        register.appendEntry(entry);
    }

    @Test(expected = AppendEntryException.class)
    public void shouldFailForMissingField() throws IOException {
        JsonNode content = mapper.readTree( postcodeRegisterItem );
        HashValue hashValue = new HashValue(HashingAlgorithm.SHA256, "abc");
        Blob blob = new Blob(hashValue, content);
        Entry entry = new Entry(1, hashValue, Instant.now(),"register:postcode", EntryType.system);
        when(index.getRecord("field:postcode", IndexNames.METADATA)).thenReturn(Optional.empty());

        register.addBlob(blob);
        register.appendEntry(entry);
    }

    @Test
    public void shouldGetRegisterMetadata() {
        RegisterMetadata registerMetadata = register.getRegisterMetadata();
        assertThat(registerMetadata.getPhase(), is("alpha"));
    }

    @Test
    public void shouldGetFields() {
        when(index.getRecord("field:postcode", IndexNames.METADATA)).thenReturn(Optional.of(fieldRecord));
        Map<String, Field> fieldsByName = register.getFieldsByName();
        assertThat(fieldsByName.size(), is(1));
        assertThat(fieldsByName.get("postcode").getText(), is("field description"));
    }

    private Blob getItem(String json) throws IOException {
        return new Blob(new ObjectMapper().readTree(json));
    }
}
