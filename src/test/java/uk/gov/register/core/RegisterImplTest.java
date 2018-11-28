package uk.gov.register.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.register.db.RecordSet;
import uk.gov.register.exceptions.AppendEntryException;
import uk.gov.register.exceptions.ItemValidationException;
import uk.gov.register.exceptions.NoSuchFieldException;
import uk.gov.register.service.EnvironmentValidator;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RegisterImplTest {
    private static ObjectMapper mapper = new ObjectMapper();

    @Mock
    private RecordSet recordSet;
    @Mock
    private ItemValidator itemValidator;
    @Mock
    private Record fieldRecord;
    @Mock
    private Record registerRecord;
    @Mock
    private EnvironmentValidator environmentValidator;
    @Mock
    private ItemStore itemStore;
    @Mock
    private EntryLog entryLog;

    private RegisterImpl register;

    private String postcodeRegisterItem = "{\"fields\":[\"postcode\"],\"phase\":\"alpha\",\"register\":\"r\",\"registry\":\"r\",\"text\":\"t\"}";

    @Before
    public void setup() throws IOException {
        register = new RegisterImpl(new RegisterId("postcode"),
                entryLog, itemStore,
                recordSet, itemValidator, environmentValidator);

        when(registerRecord.getItem()).thenReturn(getItem(postcodeRegisterItem));

        when(fieldRecord.getItem()).thenReturn(getItem("{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"postcode\",\"phase\":\"alpha\",\"register\":\"postcode\",\"text\":\"field description\"}"));

        when(recordSet.getRecord(EntryType.system, "register:postcode")).thenReturn(Optional.of(registerRecord));
        when(recordSet.getRecord(EntryType.system, "field:postcode")).thenReturn(Optional.of(fieldRecord));
    }

    @Test(expected = NoSuchFieldException.class)
    public void findMax100RecordsByKeyValueShouldFailWhenKeyDoesNotExist() {
        register.max100RecordsFacetedByKeyValue("citizen-name", "British");
    }

    @Test
    public void findMax100RecordsByKeyValueShouldReturnValueWhenKeyExists() {
        register.max100RecordsFacetedByKeyValue("postcode", "AB1 2CD");
        verify(recordSet, times(1)).findMax100RecordsByKeyValue("postcode", "AB1 2CD");
    }

    @Test(expected = AppendEntryException.class)
    public void shouldFailForUnreferencedItem() {
        Entry entry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "abc"), Instant.now(),
                "key", EntryType.user);

        when(itemStore.getItem(entry.getItemHash())).thenReturn(Optional.empty());
        register.appendEntry(entry);
    }

    @Test
    public void shouldNotFailForValidItem() throws IOException {
        JsonNode content = mapper.readTree("{\"postcode\":\"sw1a 1aa\"}");
        HashValue hashValue = new HashValue(HashingAlgorithm.SHA256, "abc");
        Item item = new Item(hashValue, content);
        Entry entry = new Entry(1, hashValue, Instant.now(),"key", EntryType.user);

        when(itemStore.getItem(entry.getItemHash())).thenReturn(Optional.of(item));

        register.appendEntry(entry);
    }

    @Test(expected = AppendEntryException.class)
    public void shouldFailIfItemValidationFails() throws IOException {
        JsonNode content = mapper.readTree("{\"foo\":\"bar\"}");
        HashValue hashValue = new HashValue(HashingAlgorithm.SHA256, "abc");
        Item item = new Item(hashValue, content);
        Entry entry = new Entry(1, hashValue, Instant.now(),"key", EntryType.user);

        when(itemStore.getItem(entry.getItemHash())).thenReturn(Optional.of(item));

        doThrow(new ItemValidationException("", content)).when(itemValidator).validateItem(any(), any(), any());

        register.appendEntry(entry);
    }

    @Test
    public void shouldGetRegisterMetadata() {
        RegisterMetadata registerMetadata = register.getRegisterMetadata();
        assertThat(registerMetadata.getPhase(), is("alpha"));
    }

    @Test
    public void shouldGetFields() {
        when(recordSet.getRecord(EntryType.system, "field:postcode")).thenReturn(Optional.of(fieldRecord));
        Map<String, Field> fieldsByName = register.getFieldsByName();
        assertThat(fieldsByName.size(), is(1));
        assertThat(fieldsByName.get("postcode").getText(), is("field description"));
    }

    private Item getItem(String json) throws IOException {
        return new Item(new ObjectMapper().readTree(json));
    }
}
