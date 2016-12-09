package uk.gov.register.store.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.db.CurrentKey;
import uk.gov.register.util.HashValue;

import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PostgresDriverTransactionalTest extends PostgresDriverTestBase {

    @Test
    public void insertItemRecordShouldNotCommitData() {
        PostgresDriverTransactional postgresDriver = new PostgresDriverTransactional(
                handle, h -> itemQueryDAO, h -> itemDAO, h -> recordQueryDAO, h -> currentKeysUpdateDAO);

        Item mockItem = mock(Item.class);
        when(mockItem.getSha256hex()).thenReturn(mock(HashValue.class));

        postgresDriver.insertItem(mockItem);
        postgresDriver.insertRecord(mockRecord("country", "DE", 1), "country");

        assertThat(items, is(empty()));
        assertThat(currentKeys, is(empty()));
    }

    @Test
    public void getAllItemsShouldAlwaysCommitStagedData() {
        when(itemQueryDAO.getAllItemsNoPagination()).thenReturn(emptyList());
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
        when(recordQueryDAO.getRecords(10, 0)).thenReturn(emptyList());
        assertStagedDataIsCommittedOnAction(postgresDriver -> postgresDriver.getRecords(10, 0));
    }

    @Test
    public void findMax100RecordsByKeyValueShouldAlwaysCommitStagedData() {
        when(recordQueryDAO.findMax100RecordsByKeyValue("name", "Germany")).thenReturn(emptyList());

        assertStagedDataIsCommittedOnAction(postgresDriver -> postgresDriver.findMax100RecordsByKeyValue("name", "Germany"));
    }

    @Test
    public void findAllEntriesOfRecordByShouldAlwaysCommitStagedData() {
        when(recordQueryDAO.findAllEntriesOfRecordBy("country", "DE")).thenReturn(emptyList());
        assertStagedDataIsCommittedOnAction(postgresDriver -> postgresDriver.findAllEntriesOfRecordBy("country", "DE"));
    }

    @Test //TODO: equals problem here
    public void getItemBySha256ShouldCommitStagedDataOnlyIfItemNotStaged() {
        ArgumentCaptor<String> hashArgumentCaptor = ArgumentCaptor.forClass(String.class);
        when(itemQueryDAO.getItemBySHA256(hashArgumentCaptor.capture()))
                .thenReturn(items.stream().filter(item -> item.getSha256hex().equals(hashArgumentCaptor.getValue())).findFirst());

        PostgresDriverTransactional postgresDriver = new PostgresDriverTransactional(
                handle, h -> itemQueryDAO, h -> itemDAO, h -> recordQueryDAO, h -> currentKeysUpdateDAO);

        items.add(new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash1"), new ObjectMapper().createObjectNode()));
        currentKeys.add(new CurrentKey("DE", 1));

        assertThat(items.size(), is(1));
        assertThat(currentKeys.size(), is(1));

        postgresDriver.insertItem(new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash2"), new ObjectMapper().createObjectNode()));
        postgresDriver.insertRecord(mockRecord("country", "VA", 2), "country");

        postgresDriver.getItemBySha256(new HashValue(HashingAlgorithm.SHA256, "itemhash2"));

        assertThat(items.size(), is(1));
        assertThat(currentKeys.size(), is(1));

        postgresDriver.getItemBySha256(new HashValue(HashingAlgorithm.SHA256, "itemhash1"));

        assertThat(items.size(), is(2));
        assertThat(currentKeys.size(), is(2));
    }

    @Test
    public void insertRecordWithSameKeyValueDoesNotStageBothCurrentKeys() {
        PostgresDriverTransactional postgresDriver = new PostgresDriverTransactional(
                handle, h -> itemQueryDAO, h -> itemDAO, h -> recordQueryDAO, h -> currentKeysUpdateDAO);

        Record record1 = mockRecord("country", "DE", 1);
        Record record2 = mockRecord("country", "VA", 2);
        Record record3 = mockRecord("country", "DE", 3);

        postgresDriver.insertRecord(record1, "country");
        postgresDriver.insertRecord(record2, "country");
        postgresDriver.insertRecord(record3, "country");

        assertThat(currentKeys, is(empty()));

        postgresDriver.getItemBySha256(mock(HashValue.class)); // force writing staged data

        assertThat(currentKeys.size(), is(2));
        assertThat(currentKeys.get(0).getKey(), is("DE"));
        assertThat(currentKeys.get(0).getEntry_number(), is(3));
        assertThat(currentKeys.get(1).getKey(), is("VA"));
        assertThat(currentKeys.get(1).getEntry_number(), is(2));
    }

    @Test
    @Ignore("needs to be replicated on PostgresRegister")
    public void entryAndItemDataShouldBeCommittedInOrder() throws Exception {
    }

    private void assertStagedDataIsCommittedOnAction(Consumer<PostgresDriverTransactional> actionToTest) {
        PostgresDriverTransactional postgresDriver = new PostgresDriverTransactional(
                handle, h -> itemQueryDAO, h -> itemDAO, h -> recordQueryDAO, h -> currentKeysUpdateDAO);

        Item item1 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash1"), new ObjectMapper().createObjectNode());
        Item item2 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash2"), new ObjectMapper().createObjectNode());

        postgresDriver.insertItem(item1);
        postgresDriver.insertItem(item2);
        postgresDriver.insertRecord(mockRecord("country", "DE", 1), "country");

        assertThat(items, is(empty()));
        assertThat(currentKeys, is(empty()));

        actionToTest.accept(postgresDriver);

        assertThat(items.size(), is(2));
        assertThat(currentKeys.size(), is(1));
    }
}
