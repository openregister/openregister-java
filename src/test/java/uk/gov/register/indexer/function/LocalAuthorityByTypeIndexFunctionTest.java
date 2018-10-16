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
        when(dataAccessLayer.getBlob(any())).thenReturn(Optional.empty());

        LocalAuthorityByTypeIndexFunction func = new LocalAuthorityByTypeIndexFunction("local-authority-by-type");
        Set<IndexKeyItemPair> resultSet = new HashSet<>();
        func.execute(h -> dataAccessLayer.getBlob(h),EntryType.user,  "LND", new HashValue(HashingAlgorithm.SHA256, "abc"), resultSet);

        assertThat(resultSet, is(empty()));
    }

    @Test
    public void executeWithKeyAndHash_shouldReturnEmptySet_whenLocalAuthorityTypeIsNotSpecifiedInItem() throws IOException {
        HashValue itemHash = new HashValue(HashingAlgorithm.SHA256, "abc");
        Blob blob = new Blob(itemHash, objectMapper.readTree("{\"local-authority-eng\":\"LND\",\"name\":\"City of London\"}"));
        when(dataAccessLayer.getBlob(itemHash)).thenReturn(Optional.of(blob));

        Set<IndexKeyItemPair> resultSet = new HashSet<>();
        func.execute(h -> dataAccessLayer.getBlob(h), EntryType.user, "LND", new HashValue(HashingAlgorithm.SHA256, "abc"), resultSet);

        assertThat(resultSet, is(empty()));
    }

    @Test
    public void executeWithKeyAndHash_shouldReturnIndexValueItemPairByLocalAuthType_whenItemExists() throws IOException {
        HashValue itemHash = new HashValue(HashingAlgorithm.SHA256, "abc");
        Blob blob = new Blob(itemHash, objectMapper.readTree("{\"local-authority-eng\":\"LND\",\"local-authority-type\":\"CC\",\"name\":\"City of London\"}"));
        when(dataAccessLayer.getBlob(itemHash)).thenReturn(Optional.of(blob));

        Set<IndexKeyItemPair> resultSet = new HashSet<>();
        func.execute(h -> dataAccessLayer.getBlob(h), EntryType.user, "LND", new HashValue(HashingAlgorithm.SHA256, "abc"), resultSet);

        assertThat(resultSet.size(), is(1));
        assertThat(resultSet, contains(new IndexKeyItemPair("CC", itemHash)));
    }

    @Test
    public void executeWithEntry_shouldReturnIndexValueItemPairByLocalAuthType_whenItemExists() throws IOException {
        HashValue itemHash = new HashValue(HashingAlgorithm.SHA256, "abc");
        Blob blob = new Blob(itemHash, objectMapper.readTree("{\"local-authority-eng\":\"LND\",\"local-authority-type\":\"CC\",\"name\":\"City of London\"}"));
        Entry entry = new Entry(1, itemHash, Instant.now(), "LND", EntryType.user);
        when(dataAccessLayer.getBlob(itemHash)).thenReturn(Optional.of(blob));

        Set<IndexKeyItemPair> resultSet = func.execute(h -> dataAccessLayer.getBlob(h), entry);

        assertThat(resultSet.size(), is(1));
        assertThat(resultSet, contains(new IndexKeyItemPair("CC", itemHash)));
    }

    @Test
    public void executeWithEntry_shouldReturnIndexValueItemPairsByLocalAuthType_whenEntryContainsMultipleItems() throws IOException {
        HashValue itemHash1 = new HashValue(HashingAlgorithm.SHA256, "abc");
        HashValue itemHash2 = new HashValue(HashingAlgorithm.SHA256, "def");
        Blob blob1 = new Blob(itemHash1, objectMapper.readTree("{\"local-authority-eng\":\"LND\",\"local-authority-type\":\"CC\",\"name\":\"City of London\"}"));
        Blob blob2 = new Blob(itemHash2, objectMapper.readTree("{\"local-authority-eng\":\"WOT\",\"local-authority-type\":\"NMD\",\"name\":\"Worthing\"}"));
        Entry entry = new Entry(1, Arrays.asList(itemHash1, itemHash2), Instant.now(), "key", EntryType.user);
        when(dataAccessLayer.getBlob(itemHash1)).thenReturn(Optional.of(blob1));
        when(dataAccessLayer.getBlob(itemHash2)).thenReturn(Optional.of(blob2));

        Set<IndexKeyItemPair> resultSet = func.execute(h -> dataAccessLayer.getBlob(h), entry);

        assertThat(resultSet.size(), is(2));
        assertThat(resultSet, containsInAnyOrder(new IndexKeyItemPair("CC", itemHash1), new IndexKeyItemPair("NMD", itemHash2)));
    }
}
