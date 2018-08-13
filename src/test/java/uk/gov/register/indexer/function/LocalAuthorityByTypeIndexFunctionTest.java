package uk.gov.register.indexer.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.core.*;
import uk.gov.register.indexer.IndexKeyItemPair;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LocalAuthorityByTypeIndexFunctionTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    LocalAuthorityByTypeIndexFunction func;
    Register register;
    DataAccessLayer dataAccessLayer;

    @Before
    public void setup(){
        register = mock(Register.class);
        dataAccessLayer = mock(DataAccessLayer.class);
        func = new LocalAuthorityByTypeIndexFunction("local-authority-by-type");
    }

    @Test
    public void executeWithKeyAndHash_shouldReturnEmptySet_whenItemDoesNotExist() {
        when(dataAccessLayer.getItem(any())).thenReturn(Optional.empty());

        LocalAuthorityByTypeIndexFunction func = new LocalAuthorityByTypeIndexFunction("local-authority-by-type");
        Set<IndexKeyItemPair> resultSet = new HashSet<>();
        func.execute(h -> dataAccessLayer.getItem(h),EntryType.user,  "LND", new HashValue(HashingAlgorithm.SHA256, "abc"), resultSet);

        assertThat(resultSet, is(empty()));
    }

    @Test
    public void executeWithKeyAndHash_shouldReturnEmptySet_whenLocalAuthorityTypeIsNotSpecifiedInItem() throws IOException {
        HashValue itemHash = new HashValue(HashingAlgorithm.SHA256, "abc");
        Item item = new Item(itemHash, objectMapper.readTree("{\"local-authority-eng\":\"LND\",\"name\":\"City of London\"}"));
        when(dataAccessLayer.getItem(itemHash)).thenReturn(Optional.of(item));

        Set<IndexKeyItemPair> resultSet = new HashSet<>();
        func.execute(h -> dataAccessLayer.getItem(h), EntryType.user, "LND", new HashValue(HashingAlgorithm.SHA256, "abc"), resultSet);

        assertThat(resultSet, is(empty()));
    }

    @Test
    public void executeWithKeyAndHash_shouldReturnIndexValueItemPairByLocalAuthType_whenItemExists() throws IOException {
        HashValue itemHash = new HashValue(HashingAlgorithm.SHA256, "abc");
        Item item = new Item(itemHash, objectMapper.readTree("{\"local-authority-eng\":\"LND\",\"local-authority-type\":\"CC\",\"name\":\"City of London\"}"));
        when(dataAccessLayer.getItem(itemHash)).thenReturn(Optional.of(item));

        Set<IndexKeyItemPair> resultSet = new HashSet<>();
        func.execute(h -> dataAccessLayer.getItem(h), EntryType.user, "LND", new HashValue(HashingAlgorithm.SHA256, "abc"), resultSet);

        assertThat(resultSet.size(), is(1));
        assertThat(resultSet, contains(new IndexKeyItemPair("CC", itemHash)));
    }

    @Test
    public void executeWithEntry_shouldReturnIndexValueItemPairByLocalAuthType_whenItemExists() throws IOException {
        HashValue itemHash = new HashValue(HashingAlgorithm.SHA256, "abc");
        Item item = new Item(itemHash, objectMapper.readTree("{\"local-authority-eng\":\"LND\",\"local-authority-type\":\"CC\",\"name\":\"City of London\"}"));
        Entry entry = new Entry(1, itemHash, Instant.now(), "LND", EntryType.user);
        when(dataAccessLayer.getItem(itemHash)).thenReturn(Optional.of(item));

        Set<IndexKeyItemPair> resultSet = func.execute(h -> dataAccessLayer.getItem(h), entry);

        assertThat(resultSet.size(), is(1));
        assertThat(resultSet, contains(new IndexKeyItemPair("CC", itemHash)));
    }

    @Test
    public void executeWithEntry_shouldReturnIndexValueItemPairsByLocalAuthType_whenEntryContainsMultipleItems() throws IOException {
        HashValue itemHash1 = new HashValue(HashingAlgorithm.SHA256, "abc");
        HashValue itemHash2 = new HashValue(HashingAlgorithm.SHA256, "def");
        Item item1 = new Item(itemHash1, objectMapper.readTree("{\"local-authority-eng\":\"LND\",\"local-authority-type\":\"CC\",\"name\":\"City of London\"}"));
        Item item2 = new Item(itemHash2, objectMapper.readTree("{\"local-authority-eng\":\"WOT\",\"local-authority-type\":\"NMD\",\"name\":\"Worthing\"}"));
        Entry entry = new Entry(1, Arrays.asList(itemHash1, itemHash2), Instant.now(), "key", EntryType.user);
        when(dataAccessLayer.getItem(itemHash1)).thenReturn(Optional.of(item1));
        when(dataAccessLayer.getItem(itemHash2)).thenReturn(Optional.of(item2));

        Set<IndexKeyItemPair> resultSet = func.execute(h -> dataAccessLayer.getItem(h), entry);

        assertThat(resultSet.size(), is(2));
        assertThat(resultSet, containsInAnyOrder(new IndexKeyItemPair("CC", itemHash1), new IndexKeyItemPair("NMD", itemHash2)));
    }
}
