package uk.gov.register.indexer.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.register.core.Entry;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.core.Register;
import uk.gov.register.indexer.IndexKeyItemPair;
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

    @Test
    public void executeWithKeyAndHash_shouldReturnEmptySet_whenItemDoesNotExist() {
        Register register = mock(Register.class);
        when(register.getItemBySha256(any())).thenReturn(Optional.empty());

        LocalAuthorityByTypeIndexFunction func = new LocalAuthorityByTypeIndexFunction(register);
        Set<IndexKeyItemPair> resultSet = new HashSet<>();
        func.execute("LND", new HashValue(HashingAlgorithm.SHA256, "abc"), resultSet);

        assertThat(resultSet, is(empty()));
    }

    @Test
    public void executeWithKeyAndHash_shouldReturnEmptySet_whenLocalAuthorityTypeIsNotSpecifiedInItem() throws IOException {
        HashValue itemHash = new HashValue(HashingAlgorithm.SHA256, "abc");
        Item item = new Item(itemHash, objectMapper.readTree("{\"local-authority-eng\":\"LND\",\"name\":\"City of London\"}"));
        Register register = mock(Register.class);
        when(register.getItemBySha256(itemHash)).thenReturn(Optional.of(item));

        LocalAuthorityByTypeIndexFunction func = new LocalAuthorityByTypeIndexFunction(register);
        Set<IndexKeyItemPair> resultSet = new HashSet<>();
        func.execute("LND", new HashValue(HashingAlgorithm.SHA256, "abc"), resultSet);

        assertThat(resultSet, is(empty()));
    }

    @Test
    public void executeWithKeyAndHash_shouldReturnIndexValueItemPairByLocalAuthType_whenItemExists() throws IOException {
        HashValue itemHash = new HashValue(HashingAlgorithm.SHA256, "abc");
        Item item = new Item(itemHash, objectMapper.readTree("{\"local-authority-eng\":\"LND\",\"local-authority-type\":\"CC\",\"name\":\"City of London\"}"));
        Register register = mock(Register.class);
        when(register.getItemBySha256(itemHash)).thenReturn(Optional.of(item));

        LocalAuthorityByTypeIndexFunction func = new LocalAuthorityByTypeIndexFunction(register);
        Set<IndexKeyItemPair> resultSet = new HashSet<>();
        func.execute("LND", new HashValue(HashingAlgorithm.SHA256, "abc"), resultSet);

        assertThat(resultSet.size(), is(1));
        assertThat(resultSet, contains(new IndexKeyItemPair("CC", itemHash)));
    }

    @Test
    public void executeWithEntry_shouldReturnIndexValueItemPairByLocalAuthType_whenItemExists() throws IOException {
        HashValue itemHash = new HashValue(HashingAlgorithm.SHA256, "abc");
        Item item = new Item(itemHash, objectMapper.readTree("{\"local-authority-eng\":\"LND\",\"local-authority-type\":\"CC\",\"name\":\"City of London\"}"));
        Entry entry = new Entry(1, itemHash, Instant.now(), "LND");
        Register register = mock(Register.class);
        when(register.getItemBySha256(itemHash)).thenReturn(Optional.of(item));

        LocalAuthorityByTypeIndexFunction func = new LocalAuthorityByTypeIndexFunction(register);
        Set<IndexKeyItemPair> resultSet = func.execute(entry);

        assertThat(resultSet.size(), is(1));
        assertThat(resultSet, contains(new IndexKeyItemPair("CC", itemHash)));
    }

    @Test
    public void executeWithEntry_shouldReturnIndexValueItemPairsByLocalAuthType_whenEntryContainsMultipleItems() throws IOException {
        HashValue itemHash1 = new HashValue(HashingAlgorithm.SHA256, "abc");
        HashValue itemHash2 = new HashValue(HashingAlgorithm.SHA256, "def");
        Item item1 = new Item(itemHash1, objectMapper.readTree("{\"local-authority-eng\":\"LND\",\"local-authority-type\":\"CC\",\"name\":\"City of London\"}"));
        Item item2 = new Item(itemHash2, objectMapper.readTree("{\"local-authority-eng\":\"WOT\",\"local-authority-type\":\"NMD\",\"name\":\"Worthing\"}"));
        Entry entry = new Entry(1, Arrays.asList(itemHash1, itemHash2), Instant.now(), "key");
        Register register = mock(Register.class);
        when(register.getItemBySha256(itemHash1)).thenReturn(Optional.of(item1));
        when(register.getItemBySha256(itemHash2)).thenReturn(Optional.of(item2));

        LocalAuthorityByTypeIndexFunction func = new LocalAuthorityByTypeIndexFunction(register);
        Set<IndexKeyItemPair> resultSet = func.execute(entry);

        assertThat(resultSet.size(), is(2));
        assertThat(resultSet, containsInAnyOrder(new IndexKeyItemPair("CC", itemHash1), new IndexKeyItemPair("NMD", itemHash2)));
    }
}
