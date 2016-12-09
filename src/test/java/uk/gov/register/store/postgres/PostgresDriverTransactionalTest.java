package uk.gov.register.store.postgres;

import org.junit.Test;
import uk.gov.register.core.Record;

import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

public class PostgresDriverTransactionalTest extends PostgresDriverTestBase {

    @Test
    public void insertRecordShouldNotCommitData() {
        PostgresDriverTransactional postgresDriver = new PostgresDriverTransactional(
                handle, h -> recordQueryDAO, h -> currentKeysUpdateDAO);

        postgresDriver.insertRecord(mockRecord("country", "DE", 1), "country");

        assertThat(currentKeys, is(empty()));
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

    @Test
    public void insertRecordWithSameKeyValueDoesNotStageBothCurrentKeys() {
        PostgresDriverTransactional postgresDriver = new PostgresDriverTransactional(
                handle, h -> recordQueryDAO, h -> currentKeysUpdateDAO);

        Record record1 = mockRecord("country", "DE", 1);
        Record record2 = mockRecord("country", "VA", 2);
        Record record3 = mockRecord("country", "DE", 3);

        postgresDriver.insertRecord(record1, "country");
        postgresDriver.insertRecord(record2, "country");
        postgresDriver.insertRecord(record3, "country");

        assertThat(currentKeys, is(empty()));

        postgresDriver.getTotalRecords(); // force writing staged data

        assertThat(currentKeys.size(), is(2));
        assertThat(currentKeys.get(0).getKey(), is("DE"));
        assertThat(currentKeys.get(0).getEntry_number(), is(3));
        assertThat(currentKeys.get(1).getKey(), is("VA"));
        assertThat(currentKeys.get(1).getEntry_number(), is(2));
    }

    private void assertStagedDataIsCommittedOnAction(Consumer<PostgresDriverTransactional> actionToTest) {
        PostgresDriverTransactional postgresDriver = new PostgresDriverTransactional(
                handle, h -> recordQueryDAO, h -> currentKeysUpdateDAO);

        postgresDriver.insertRecord(mockRecord("country", "DE", 1), "country");

        assertThat(currentKeys, is(empty()));

        actionToTest.accept(postgresDriver);

        assertThat(currentKeys.size(), is(1));
    }
}
