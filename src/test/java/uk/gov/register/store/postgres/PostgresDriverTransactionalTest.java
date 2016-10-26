package uk.gov.register.store.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.assertj.core.error.ShouldBeInSameYear;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.skife.jdbi.v2.Handle;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.db.*;
import uk.gov.register.views.RegisterProof;
import uk.gov.verifiablelog.VerifiableLog;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PostgresDriverTransactionalTest {

    List<Entry> entries;
    List<Item> items;
    List<CurrentKey> currentKeys;

    EntryQueryDAO entryQueryDAO;
    EntryDAO entryDAO;
    ItemQueryDAO itemQueryDAO;
    ItemDAO itemDAO;
    RecordQueryDAO recordQueryDAO;
    CurrentKeysUpdateDAO currentKeysUpdateDAO;

    @Mock
    Handle handle;
    @Mock
    MemoizationStore memoizationStore;

    ArgumentCaptor<Collection> argumentCaptor = ArgumentCaptor.forClass(Collection.class);

    @Before
    public void setup() {
        entries = new ArrayList<>();
        items = new ArrayList<>();
        currentKeys = new ArrayList<>();

        entryQueryDAO = mock(EntryQueryDAO.class);
        entryDAO = mock(EntryDAO.class);
        itemQueryDAO = mock(ItemQueryDAO.class);
        itemDAO = mock(ItemDAO.class);
        recordQueryDAO = mock(RecordQueryDAO.class);
        currentKeysUpdateDAO = mock(CurrentKeysUpdateDAO.class);

        mockEntryDAOInsert();
        mockItemDAOInsert();
        mockCurrentKeysUpdateDAOInsert();
    }

    @Test
    public void insertItemEntryRecordShouldNotCommitData() {
        PostgresDriverTransactional postgresDriver = new PostgresDriverTransactional(
                handle, memoizationStore, h -> entryQueryDAO, h -> entryDAO, h -> itemQueryDAO, h -> itemDAO, h -> recordQueryDAO, h -> currentKeysUpdateDAO);

        postgresDriver.insertItem(mock(Item.class));
        postgresDriver.insertEntry(mock(Entry.class));
        postgresDriver.insertEntry(mock(Entry.class));
        postgresDriver.insertRecord(mockRecord("country", "DE", 1), "country");

        assertThat(items, is(empty()));
        assertThat(entries, is(empty()));
        assertThat(currentKeys, is(empty()));
    }

    @Test
    public void getEntryShouldAlwaysCommitStagedData() {
        when(entryQueryDAO.findByEntryNumber(1)).thenReturn(Optional.empty());
        assertStagedDataIsCommittedOnAction(postgresDriver -> postgresDriver.getEntry(1));
    }

    @Test
    public void getEntriesShouldAlwaysCommitStagedData() {
        when(entryQueryDAO.getEntries(1, 10)).thenReturn(asList());
        assertStagedDataIsCommittedOnAction(postgresDriver -> postgresDriver.getEntries(1, 10));
    }

    @Test
    public void getAllEntriesShouldAlwaysCommitStagedData() {
        when(entryQueryDAO.getAllEntriesNoPagination()).thenReturn(asList());
        assertStagedDataIsCommittedOnAction(PostgresDriverTransactional::getAllEntries);
    }

    @Test
    public void getTotalEntriesShouldAlwaysCommitStagedData() {
        when(entryQueryDAO.getTotalEntries()).thenReturn(10);
        assertStagedDataIsCommittedOnAction(PostgresDriverTransactional::getTotalEntries);
    }

    @Test
    public void getLastUpdatedTimeShouldAlwaysCommitStagedData() {
        when(entryQueryDAO.getLastUpdatedTime()).thenReturn(Optional.empty());
        assertStagedDataIsCommittedOnAction(PostgresDriverTransactional::getLastUpdatedTime);
    }

    @Test
    public void getAllItemsShouldAlwaysCommitStagedData() {
        when(itemQueryDAO.getAllItemsNoPagination()).thenReturn(asList());
        assertStagedDataIsCommittedOnAction(PostgresDriverTransactional::getAllItems);
    }

    @Test
    public void getRecordShouldAlwaysCommitStagedData() {
        when(recordQueryDAO.findByPrimaryKey("DE")).thenReturn(Optional.empty());
        assertStagedDataIsCommittedOnAction(postgresDriver -> postgresDriver.getRecord("DE"));
    }

    @Test
    public void getTotalRecordsShouldAlwaysCommitStagedData() {
        when(recordQueryDAO.getTotalRecords()).thenReturn(10);
        assertStagedDataIsCommittedOnAction(PostgresDriverTransactional::getTotalRecords);
    }

    @Test
    public void getRecordsShouldAlwaysCommitStagedData() {
        when(recordQueryDAO.getRecords(10, 0)).thenReturn(asList());
        assertStagedDataIsCommittedOnAction(postgresDriver -> postgresDriver.getRecords(10, 0));
    }

    @Test
    public void findMax100RecordsByKeyValueShouldAlwaysCommitStagedData() {
        when(recordQueryDAO.findMax100RecordsByKeyValue("name", "Germany")).thenReturn(asList());
        assertStagedDataIsCommittedOnAction(postgresDriver -> postgresDriver.findMax100RecordsByKeyValue("name", "Germany"));
    }

    @Test
    public void findAllEntriesOfRecordByShouldAlwaysCommitStagedData() {
        when(recordQueryDAO.findAllEntriesOfRecordBy("country", "DE")).thenReturn(asList());
        assertStagedDataIsCommittedOnAction(postgresDriver -> postgresDriver.findAllEntriesOfRecordBy("country", "DE"));
    }

    @Test
    public void withVerifiableLogShouldAlwaysCommitStagedData() {
        Function<VerifiableLog, RegisterProof> func = mock(Function.class);
        when(func.apply(mock(VerifiableLog.class))).thenReturn(mock(RegisterProof.class));
        assertStagedDataIsCommittedOnAction(postgresDriver -> postgresDriver.withVerifiableLog(verifiableLog -> func));
    }

    @Test
    public void getItemBySha256ShouldCommitStagedDataOnlyIfItemNotStaged() {
        ArgumentCaptor<String> hashArgumentCaptor = ArgumentCaptor.forClass(String.class);
        when(itemQueryDAO.getItemBySHA256(hashArgumentCaptor.capture()))
                .thenReturn(items.stream().filter(item -> item.getSha256hex().equals(hashArgumentCaptor.getValue())).findFirst());

        PostgresDriverTransactional postgresDriver = new PostgresDriverTransactional(
                handle, memoizationStore, h -> entryQueryDAO, h -> entryDAO, h -> itemQueryDAO, h -> itemDAO, h -> recordQueryDAO, h -> currentKeysUpdateDAO);

        items.add(new Item("itemhash1", new ObjectMapper().createObjectNode()));
        entries.add(mock(Entry.class));
        currentKeys.add(new CurrentKey("DE", 1));

        assertThat(items.size(), is(1));
        assertThat(entries.size(), is(1));
        assertThat(currentKeys.size(), is(1));

        postgresDriver.insertItem(new Item("itemhash2", new ObjectMapper().createObjectNode()));
        postgresDriver.insertEntry(mock(Entry.class));
        postgresDriver.insertRecord(mockRecord("country", "VA", 2), "country");

        postgresDriver.getItemBySha256("itemhash2");

        assertThat(items.size(), is(1));
        assertThat(entries.size(), is(1));
        assertThat(currentKeys.size(), is(1));

        postgresDriver.getItemBySha256("itemhash1");

        assertThat(items.size(), is(2));
        assertThat(entries.size(), is(2));
        assertThat(currentKeys.size(), is(2));

    }

    @Test
    public void insertRecordWithSameKeyValueDoesNotStageBothCurrentKeys() {
        when(entryQueryDAO.getAllEntriesNoPagination()).thenReturn(asList());
        PostgresDriverTransactional postgresDriver = new PostgresDriverTransactional(
                handle, memoizationStore, h -> entryQueryDAO, h -> entryDAO, h -> itemQueryDAO, h -> itemDAO, h -> recordQueryDAO, h -> currentKeysUpdateDAO);

        Record record1 = mockRecord("country", "DE", 1);
        Record record2 = mockRecord("country", "VA", 2);
        Record record3 = mockRecord("country", "DE", 3);

        postgresDriver.insertRecord(record1, "country");
        postgresDriver.insertRecord(record2, "country");
        postgresDriver.insertRecord(record3, "country");

        assertThat(currentKeys, is(empty()));

        postgresDriver.getAllEntries();

        assertThat(currentKeys.size(), is(2));
        assertThat(currentKeys.get(0).getKey(), is("DE"));
        assertThat(currentKeys.get(0).getEntry_number(), is(3));
        assertThat(currentKeys.get(1).getKey(), is("VA"));
        assertThat(currentKeys.get(1).getEntry_number(), is(2));
    }

    @Test
    public void entryAndItemDataShouldBeCommittedInOrder() {
        when(entryQueryDAO.getAllEntriesNoPagination()).thenReturn(asList());
        PostgresDriverTransactional postgresDriver = new PostgresDriverTransactional(
                handle, memoizationStore, h -> entryQueryDAO, h -> entryDAO, h -> itemQueryDAO, h -> itemDAO, h -> recordQueryDAO, h -> currentKeysUpdateDAO);

        Item item1 = new Item("itemhash1", new ObjectMapper().createObjectNode());
        Item item2 = new Item("itemhash2", new ObjectMapper().createObjectNode());
        Item item3 = new Item("itemhash3", new ObjectMapper().createObjectNode());
        Entry entry1 = new Entry(1, "itemhash1", Instant.now());
        Entry entry2 = new Entry(2, "itemhash2", Instant.now());
        Entry entry3 = new Entry(3, "itemhash3", Instant.now());

        postgresDriver.insertItem(item1);
        postgresDriver.insertItem(item2);
        postgresDriver.insertItem(item3);
        postgresDriver.insertEntry(entry1);
        postgresDriver.insertEntry(entry2);
        postgresDriver.insertEntry(entry3);

        postgresDriver.getAllEntries();

        assertThat(items, contains(item1, item2, item3));
        assertThat(entries, contains(entry1, entry2, entry3));
    }

    private void assertStagedDataIsCommittedOnAction(Consumer<PostgresDriverTransactional> actionToTest) {
        PostgresDriverTransactional postgresDriver = new PostgresDriverTransactional(
                handle, memoizationStore, h -> entryQueryDAO, h -> entryDAO, h -> itemQueryDAO, h -> itemDAO, h -> recordQueryDAO, h -> currentKeysUpdateDAO);

        postgresDriver.insertItem(mock(Item.class));
        postgresDriver.insertItem(mock(Item.class));
        postgresDriver.insertEntry(mock(Entry.class));
        postgresDriver.insertRecord(mockRecord("country", "DE", 1), "country");

        assertThat(items, is(empty()));
        assertThat(entries, is(empty()));
        assertThat(currentKeys, is(empty()));

        actionToTest.accept(postgresDriver);

        assertThat(items.size(), is(2));
        assertThat(entries.size(), is(1));
        assertThat(currentKeys.size(), is(1));
    }

    private Record mockRecord(String registerName, String key, Integer entryNumber) {
        Entry entry = mock(Entry.class);
        Item item = mock(Item.class);
        when(entry.getEntryNumber()).thenReturn(entryNumber);
        when(item.getKey(registerName)).thenReturn(key);
        return new Record(entry, item);
    }

    private void mockEntryDAOInsert() {
        Mockito.doAnswer(invocation -> {
            entries.addAll(argumentCaptor.getValue());
            return null;
        }).when(entryDAO).insertInBatch(argumentCaptor.capture());
    }

    private void mockItemDAOInsert() {
        Mockito.doAnswer(invocation -> {
            items.addAll(argumentCaptor.getValue());
            return null;
        }).when(itemDAO).insertInBatch(argumentCaptor.capture());
    }

    private void mockCurrentKeysUpdateDAOInsert() {
        Mockito.doAnswer(invocation -> {
            currentKeys.addAll(argumentCaptor.getValue());
            return null;
        }).when(currentKeysUpdateDAO).writeCurrentKeys(argumentCaptor.capture());
    }
}
