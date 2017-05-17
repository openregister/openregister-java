package uk.gov.register.store.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.core.Entry;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.db.*;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.register.core.HashingAlgorithm.SHA256;

public class PostgresDataAccessLayerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    InMemoryEntryDAO entryQueryDAO;
    InMemoryEntryItemDAO entryItemDAO;
    IndexQueryDAO indexQueryDAO;
    InMemoryItemDAO itemDAO;
    RecordQueryDAO recordQueryDAO;
    InMemoryCurrentKeysUpdateDAO currentKeysUpdateDAO;

    private List<Entry> entries;
    private Map<HashValue, Item> itemMap;
    private Map<String, Integer> currentKeys;

    private PostgresDataAccessLayer dataAccessLayer;

    private final Entry entry1 = new Entry(1, new HashValue(SHA256, "abc"), Instant.ofEpochMilli(123), "key1");
    private final Entry entry2 = new Entry(2, new HashValue(SHA256, "def"), Instant.ofEpochMilli(124), "key2");
    private Item item1;
    private Item item2;

    @Before
    public void setUp() throws Exception {
        entries = new ArrayList<>();
        itemMap = new HashMap<>();
        currentKeys = new HashMap<>();

        entryQueryDAO = new InMemoryEntryDAO(entries);
        entryItemDAO = new InMemoryEntryItemDAO();
        indexQueryDAO = mock(IndexQueryDAO.class);
        itemDAO = new InMemoryItemDAO(itemMap, new InMemoryEntryDAO(entries));
        recordQueryDAO = mock(RecordQueryDAO.class);
        currentKeysUpdateDAO = new InMemoryCurrentKeysUpdateDAO(currentKeys);

        dataAccessLayer = new PostgresDataAccessLayer(entryQueryDAO, indexQueryDAO, entryQueryDAO, entryItemDAO,
                itemDAO, itemDAO, recordQueryDAO, currentKeysUpdateDAO);

        item1 = new Item(new HashValue(SHA256, "abcd"), objectMapper.readTree("{}"));
        item2 = new Item(new HashValue(SHA256, "jkl1"), objectMapper.readTree("{}"));
    }

    @Test
    public void appendEntry_shouldNotCommitData() {
        dataAccessLayer.appendEntry(new Entry(1, new HashValue(SHA256, "abc"), Instant.ofEpochMilli(123), "key1"));
        dataAccessLayer.appendEntry(new Entry(2, new HashValue(SHA256, "def"), Instant.ofEpochMilli(124), "key2"));

        assertThat(entries, is(empty()));
    }

    @Test
    public void getEntry_shouldGetFromStagedDataIfNeeded() throws Exception {
        Entry entry1 = new Entry(1, new HashValue(SHA256, "abc"), Instant.ofEpochMilli(123), "key1");
        dataAccessLayer.appendEntry(entry1);
        dataAccessLayer.appendEntry(new Entry(2, new HashValue(SHA256, "def"), Instant.ofEpochMilli(124), "key2"));

        assertThat(dataAccessLayer.getEntry(1), equalTo(Optional.of(entry1)));
    }

    @Test
    public void getEntries_shouldGetFromStagedDataIfNeeded() throws Exception {
        dataAccessLayer.appendEntry(entry1);
        dataAccessLayer.appendEntry(entry2);

        assertThat(dataAccessLayer.getEntries(1, 2), equalTo(ImmutableList.of(entry1, entry2)));
    }

    @Test
    public void getAllEntries_shouldGetFromStagedDataIfNeeded() throws Exception {
        dataAccessLayer.appendEntry(entry1);
        dataAccessLayer.appendEntry(entry2);

        assertThat(dataAccessLayer.getAllEntries(), equalTo(ImmutableList.of(entry1, entry2)));
    }

    @Test
    public void getTotalEntries_shouldCountStagedDataWithoutCommitting() throws Exception {
        // existing entry in backing store
        entries.add(entry1);
        // entry in staging area
        dataAccessLayer.appendEntry(entry2);

        int totalEntries = dataAccessLayer.getTotalEntries();

        assertThat(totalEntries, equalTo(2)); // we counted the staged entry
        assertThat(entries, equalTo(singletonList(entry1))); // but we didn't commit it to backing store
    }

    @Test
    public void getLastTimeUpdated_shouldGetFromStagedDataIfNeeded() throws Exception {
        dataAccessLayer.appendEntry(entry1);
        dataAccessLayer.appendEntry(entry2);

        assertThat(dataAccessLayer.getLastUpdatedTime(), equalTo(Optional.of(entry2.getTimestamp())));
    }

    @Test
    public void putItem_shouldNotCommitData() {
        dataAccessLayer.putItem(item1);
        dataAccessLayer.putItem(item2);

        assertThat(itemMap.entrySet(), is(empty()));
    }

    @Test
    public void getAllItems_shouldGetFromStagedDataIfNeeded() throws Exception {
        dataAccessLayer.putItem(item1);
        dataAccessLayer.putItem(item2);

        assertThat(dataAccessLayer.getAllItems(), is(iterableWithSize(2)));
    }

    @Test
    public void getItemBySha256_shouldGetFromStagedDataIfNeeded() throws Exception {
        dataAccessLayer.putItem(item1);
        dataAccessLayer.putItem(item2);

        assertThat(dataAccessLayer.getItemBySha256(item1.getSha256hex()), is(Optional.of(item1)));
    }

    @Test
    public void getIterator_shouldGetFromStagedDataIfNeeded() throws Exception {
        dataAccessLayer.putItem(item1);
        dataAccessLayer.putItem(item2);
        entries.add(new Entry(1, item1.getSha256hex(), Instant.ofEpochSecond(12345), "12345"));
        entries.add(new Entry(2, item2.getSha256hex(), Instant.ofEpochSecond(54321), "54321"));

        List<Item> items = newArrayList(dataAccessLayer.getItemIterator());
        assertThat(items, is(asList(item1, item2)));
    }

    @Test
    public void getIteratorRange_shouldGetFromStagedDataIfNeeded() throws Exception {
        dataAccessLayer.putItem(item1);
        dataAccessLayer.putItem(item2);
        entries.add(new Entry(1, item1.getSha256hex(), Instant.ofEpochSecond(12345), "12345"));
        entries.add(new Entry(2, item2.getSha256hex(), Instant.ofEpochSecond(54321), "54321"));

        List<Item> items = newArrayList(dataAccessLayer.getItemIterator(1,2));
        assertThat(items, is(singletonList(item2)));
    }

    @Test
    public void updateRecordIndex_shouldNotCommitChanges() throws Exception {
        dataAccessLayer.updateRecordIndex(new Entry(5, new HashValue(HashingAlgorithm.SHA256, "foo"), Instant.now(), "foo"));

        assertThat(currentKeys.entrySet(), is(empty()));
    }

    @Test
    public void getRecord_shouldCauseCheckpoint() {
        dataAccessLayer.updateRecordIndex(new Entry(5, new HashValue(HashingAlgorithm.SHA256, "foo"), Instant.now(), "foo"));

        Optional<Record> ignored = dataAccessLayer.getRecord("foo");

        // ignore the result, but check that we flushed out to currentKeys
        assertThat(currentKeys.get("foo"), is(5));
    }

    @Test
    public void getRecords_shouldCauseCheckpoint() {
        dataAccessLayer.updateRecordIndex(new Entry(5, new HashValue(HashingAlgorithm.SHA256, "foo"), Instant.now(), "foo"));

        List<Record> ignored = dataAccessLayer.getRecords(1,0);

        // ignore the result, but check that we flushed out to currentKeys
        assertThat(currentKeys.get("foo"), is(5));
    }

    @Test
    public void findMax100RecordsByKeyValue_shouldCauseCheckpoint() {
        dataAccessLayer.updateRecordIndex(new Entry(5, new HashValue(HashingAlgorithm.SHA256, "foo"), Instant.now(), "foo"));

        List<Record> ignored = dataAccessLayer.findMax100RecordsByKeyValue("foo", "bar");

        // ignore the result, but check that we flushed out to currentKeys
        assertThat(currentKeys.get("foo"), is(5));
    }

    @Test
    public void findAllEntriesOfRecordBy_shouldCauseCheckpoint() {
        dataAccessLayer.updateRecordIndex(new Entry(5, new HashValue(HashingAlgorithm.SHA256, "foo"), Instant.now(), "foo"));

        Collection<Entry> ignored = dataAccessLayer.findAllEntriesOfRecordBy("bar");

        // ignore the result, but check that we flushed out to currentKeys
        assertThat(currentKeys.get("foo"), is(5));
    }

    @Test
    public void getTotalRecords_shouldCauseCheckpoint() {
        dataAccessLayer.updateRecordIndex(new Entry(5, new HashValue(HashingAlgorithm.SHA256, "foo"), Instant.now(), "foo"));

        int ignored = dataAccessLayer.getTotalRecords();

        // ignore the result, but check that we flushed out to currentKeys
        assertThat(currentKeys.get("foo"), is(5));
    }

    @Test
    public void insertRecordWithSameKeyValueDoesNotStageBothCurrentKeys() {
        dataAccessLayer.updateRecordIndex(new Entry(1, new HashValue(HashingAlgorithm.SHA256, "de"), Instant.now(), "DE"));
        dataAccessLayer.updateRecordIndex(new Entry(2, new HashValue(HashingAlgorithm.SHA256, "va"), Instant.now(), "VA"));
        dataAccessLayer.updateRecordIndex(new Entry(3, new HashValue(HashingAlgorithm.SHA256, "de"), Instant.now(), "DE"));

        assertThat(currentKeys.entrySet(), is(empty()));

        dataAccessLayer.checkpoint(); // force writing staged data

        assertThat(currentKeys.size(), is(2));
        assertThat(currentKeys.get("DE"), is(3));
        assertThat(currentKeys.get("VA"), is(2));
    }

    @Test
    public void whenInserting_shouldUpdateRecordCount() throws Exception {
        dataAccessLayer.updateRecordIndex(new Entry(1, new HashValue(HashingAlgorithm.SHA256, "de"), Instant.now(), "DE"));
        dataAccessLayer.updateRecordIndex(new Entry(2, new HashValue(HashingAlgorithm.SHA256, "va"), Instant.now(), "VA"));
        dataAccessLayer.updateRecordIndex(new Entry(1, new HashValue(HashingAlgorithm.SHA256, "de"), Instant.now(), "DE"));
        dataAccessLayer.checkpoint(); // force writing staged data

        assertThat(currentKeys.size(), is(2));
        assertThat(currentKeysUpdateDAO.getTotalRecords(), is(2));
        dataAccessLayer.updateRecordIndex(new Entry(4, new HashValue(HashingAlgorithm.SHA256, "cz"), Instant.now(), "CZ"));
        dataAccessLayer.updateRecordIndex(new Entry(5, new HashValue(HashingAlgorithm.SHA256, "tv"), Instant.now(), "TV"));
        dataAccessLayer.checkpoint();

        assertThat(currentKeys.size(), is(4));
        assertThat(currentKeysUpdateDAO.getTotalRecords(), is(4));
    }

    @Test
    public void updateRecordIndex_shouldUpdateTotalRecords_whenKeyExistsInDatabaseAndEntryContainsNoItems() {
        dataAccessLayer.updateRecordIndex(new Entry(1, new HashValue(HashingAlgorithm.SHA256, "de"), Instant.now(), "DE"));
        dataAccessLayer.updateRecordIndex(new Entry(2, new HashValue(HashingAlgorithm.SHA256, "cz"), Instant.now(), "CZ"));
        dataAccessLayer.checkpoint();

        assertThat(currentKeys.size(), is(2));
        assertThat(currentKeysUpdateDAO.getTotalRecords(), is(2));

        dataAccessLayer.updateRecordIndex(new Entry(3, Collections.emptyList(), Instant.now(), "DE"));
        dataAccessLayer.checkpoint();

        assertThat(currentKeys.size(), is(2));
        assertThat(currentKeys.containsKey("DE"), is(true));
        assertThat(currentKeysUpdateDAO.getTotalRecords(), is(1));
    }

    @Test
    public void updateRecordIndex_shouldUpdateCurrentKeysAndTotalRecords_whenKeyExistsInDatabase() {
        dataAccessLayer.updateRecordIndex(new Entry(1, new HashValue(HashingAlgorithm.SHA256, "de"), Instant.now(), "DE"));
        dataAccessLayer.updateRecordIndex(new Entry(2, new HashValue(HashingAlgorithm.SHA256, "va"), Instant.now(), "VA"));
        dataAccessLayer.updateRecordIndex(new Entry(3, new HashValue(HashingAlgorithm.SHA256, "de"), Instant.now(), "DE"));
        dataAccessLayer.checkpoint();

        assertThat(currentKeys.size(), is(2));
        assertThat(currentKeysUpdateDAO.getTotalRecords(), is(2));

        dataAccessLayer.updateRecordIndex(new Entry(4, Collections.emptyList(), Instant.now(), "DE"));
        dataAccessLayer.checkpoint();

        assertThat(currentKeys.size(), is(2));
        assertThat(currentKeys.containsKey("DE"), is(true));
        assertThat(currentKeysUpdateDAO.getTotalRecords(), is(1));
    }

    @Test
    public void updateRecordIndex_shouldNotUpdateCurrentKeysAndTotalRecords_whenKeyExistsInStagedData() {
        dataAccessLayer.updateRecordIndex(new Entry(1, new HashValue(HashingAlgorithm.SHA256, "de"), Instant.now(), "DE"));
        dataAccessLayer.updateRecordIndex(new Entry(2, new HashValue(HashingAlgorithm.SHA256, "va"), Instant.now(), "VA"));
        dataAccessLayer.updateRecordIndex(new Entry(3, Collections.emptyList(), Instant.now(), "DE"));
        dataAccessLayer.checkpoint();

        assertThat(currentKeys.size(), is(2));
        assertThat(currentKeys.containsKey("DE"), is(true));
        assertThat(currentKeysUpdateDAO.getTotalRecords(), is(1));
    }

    @Test
    public void updateRecordIndex_shouldNotUpdateCurrentKeysAndTotalRecords_whenKeyDoesNotExistInStagedDataOrDatabase() {
        dataAccessLayer.updateRecordIndex(new Entry(1, new HashValue(HashingAlgorithm.SHA256, "de"), Instant.now(), "DE"));
        dataAccessLayer.updateRecordIndex(new Entry(2, new HashValue(HashingAlgorithm.SHA256, "va"), Instant.now(), "VA"));
        dataAccessLayer.checkpoint();

        assertThat(currentKeys.size(), is(2));
        assertThat(currentKeysUpdateDAO.getTotalRecords(), is(2));

        dataAccessLayer.updateRecordIndex(new Entry(3, Collections.emptyList(), Instant.now(), "CZ"));
        dataAccessLayer.checkpoint();

        assertThat(currentKeys.size(), is(3));
        assertThat(currentKeys.containsKey("CZ"), is(true));
        assertThat(currentKeysUpdateDAO.getTotalRecords(), is(2));
    }
}
