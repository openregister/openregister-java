package uk.gov.register.db;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.core.Entry;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Record;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class TransactionalRecordIndexTest {
    private TransactionalRecordIndex recordIndex;
    InMemoryCurrentKeysUpdateDAO currentKeysUpdateDAO;
    private Map<String, Integer> currentKeys;
    @Mock
    RecordQueryDAO recordQueryDAO;

    @Before
    public void setUp() throws Exception {
        currentKeys = new HashMap<>();
        currentKeysUpdateDAO = new InMemoryCurrentKeysUpdateDAO(currentKeys);
        recordIndex = new TransactionalRecordIndex(recordQueryDAO, currentKeysUpdateDAO);
    }

    @Test
    public void updateRecordIndex_shouldNotCommitChanges() throws Exception {
        recordIndex.updateRecordIndex(new Entry(5, new HashValue(HashingAlgorithm.SHA256, "foo"), Instant.now(), "foo"));

        assertThat(currentKeys.entrySet(), is(empty()));
    }

    @Test
    public void getRecord_shouldCauseCheckpoint() {
        recordIndex.updateRecordIndex(new Entry(5, new HashValue(HashingAlgorithm.SHA256, "foo"), Instant.now(), "foo"));

        Optional<Record> ignored = recordIndex.getRecord("foo");

        // ignore the result, but check that we flushed out to currentKeys
        assertThat(currentKeys.get("foo"), is(5));
    }

    @Test
    public void getRecords_shouldCauseCheckpoint() {
        recordIndex.updateRecordIndex(new Entry(5, new HashValue(HashingAlgorithm.SHA256, "foo"), Instant.now(), "foo"));

        List<Record> ignored = recordIndex.getRecords(1,0);

        // ignore the result, but check that we flushed out to currentKeys
        assertThat(currentKeys.get("foo"), is(5));
    }

    @Test
    public void findMax100RecordsByKeyValue_shouldCauseCheckpoint() {
        recordIndex.updateRecordIndex(new Entry(5, new HashValue(HashingAlgorithm.SHA256, "foo"), Instant.now(), "foo"));

        List<Record> ignored = recordIndex.findMax100RecordsByKeyValue("foo", "bar");

        // ignore the result, but check that we flushed out to currentKeys
        assertThat(currentKeys.get("foo"), is(5));
    }

    @Test
    public void findAllEntriesOfRecordBy_shouldCauseCheckpoint() {
        recordIndex.updateRecordIndex(new Entry(5, new HashValue(HashingAlgorithm.SHA256, "foo"), Instant.now(), "foo"));

        Collection<Entry> ignored = recordIndex.findAllEntriesOfRecordBy("bar");

        // ignore the result, but check that we flushed out to currentKeys
        assertThat(currentKeys.get("foo"), is(5));
    }

    @Test
    public void getTotalRecords_shouldCauseCheckpoint() {
        recordIndex.updateRecordIndex(new Entry(5, new HashValue(HashingAlgorithm.SHA256, "foo"), Instant.now(), "foo"));

        int ignored = recordIndex.getTotalRecords();

        // ignore the result, but check that we flushed out to currentKeys
        assertThat(currentKeys.get("foo"), is(5));
    }

    @Test
    public void insertRecordWithSameKeyValueDoesNotStageBothCurrentKeys() {
        recordIndex.updateRecordIndex(new Entry(1, new HashValue(HashingAlgorithm.SHA256, "de"), Instant.now(), "DE"));
        recordIndex.updateRecordIndex(new Entry(2, new HashValue(HashingAlgorithm.SHA256, "va"), Instant.now(), "VA"));
        recordIndex.updateRecordIndex(new Entry(3, new HashValue(HashingAlgorithm.SHA256, "de"), Instant.now(), "DE"));

        assertThat(currentKeys.entrySet(), is(empty()));

        recordIndex.checkpoint(); // force writing staged data

        assertThat(currentKeys.size(), is(2));
        assertThat(currentKeys.get("DE"), is(3));
        assertThat(currentKeys.get("VA"), is(2));
    }

    @Test
    public void whenInserting_shouldUpdateRecordCount() throws Exception {
        recordIndex.updateRecordIndex(new Entry(1, new HashValue(HashingAlgorithm.SHA256, "de"), Instant.now(), "DE"));
        recordIndex.updateRecordIndex(new Entry(2, new HashValue(HashingAlgorithm.SHA256, "va"), Instant.now(), "VA"));
        recordIndex.updateRecordIndex(new Entry(1, new HashValue(HashingAlgorithm.SHA256, "de"), Instant.now(), "DE"));
        recordIndex.checkpoint(); // force writing staged data

        assertThat(currentKeys.size(), is(2));
        assertThat(currentKeysUpdateDAO.getTotalRecords(), is(2));
        recordIndex.updateRecordIndex(new Entry(4, new HashValue(HashingAlgorithm.SHA256, "cz"), Instant.now(), "CZ"));
        recordIndex.updateRecordIndex(new Entry(5, new HashValue(HashingAlgorithm.SHA256, "tv"), Instant.now(), "TV"));
        recordIndex.checkpoint();

        assertThat(currentKeys.size(), is(4));
        assertThat(currentKeysUpdateDAO.getTotalRecords(), is(4));
    }

    @Test
    public void updateRecordIndex_shouldUpdateTotalRecords_whenKeyExistsInDatabaseAndEntryContainsNoItems() {
        recordIndex.updateRecordIndex(new Entry(1, new HashValue(HashingAlgorithm.SHA256, "de"), Instant.now(), "DE"));
        recordIndex.updateRecordIndex(new Entry(2, new HashValue(HashingAlgorithm.SHA256, "cz"), Instant.now(), "CZ"));
        recordIndex.checkpoint();

        assertThat(currentKeys.size(), is(2));
        assertThat(currentKeysUpdateDAO.getTotalRecords(), is(2));

        recordIndex.updateRecordIndex(new Entry(3, Collections.emptyList(), Instant.now(), "DE"));
        recordIndex.checkpoint();

        assertThat(currentKeys.size(), is(2));
        assertThat(currentKeys.containsKey("DE"), is(true));
        assertThat(currentKeysUpdateDAO.getTotalRecords(), is(1));
    }

    @Test
    public void updateRecordIndex_shouldUpdateCurrentKeysAndTotalRecords_whenKeyExistsInDatabase() {
        recordIndex.updateRecordIndex(new Entry(1, new HashValue(HashingAlgorithm.SHA256, "de"), Instant.now(), "DE"));
        recordIndex.updateRecordIndex(new Entry(2, new HashValue(HashingAlgorithm.SHA256, "va"), Instant.now(), "VA"));
        recordIndex.updateRecordIndex(new Entry(3, new HashValue(HashingAlgorithm.SHA256, "de"), Instant.now(), "DE"));
        recordIndex.checkpoint();

        assertThat(currentKeys.size(), is(2));
        assertThat(currentKeysUpdateDAO.getTotalRecords(), is(2));

        recordIndex.updateRecordIndex(new Entry(4, Collections.emptyList(), Instant.now(), "DE"));
        recordIndex.checkpoint();

        assertThat(currentKeys.size(), is(2));
        assertThat(currentKeys.containsKey("DE"), is(true));
        assertThat(currentKeysUpdateDAO.getTotalRecords(), is(1));
    }

    @Test
    public void updateRecordIndex_shouldNotUpdateCurrentKeysAndTotalRecords_whenKeyExistsInStagedData() {
        recordIndex.updateRecordIndex(new Entry(1, new HashValue(HashingAlgorithm.SHA256, "de"), Instant.now(), "DE"));
        recordIndex.updateRecordIndex(new Entry(2, new HashValue(HashingAlgorithm.SHA256, "va"), Instant.now(), "VA"));
        recordIndex.updateRecordIndex(new Entry(3, Collections.emptyList(), Instant.now(), "DE"));
        recordIndex.checkpoint();

        assertThat(currentKeys.size(), is(2));
        assertThat(currentKeys.containsKey("DE"), is(true));
        assertThat(currentKeysUpdateDAO.getTotalRecords(), is(1));
    }

    @Test
    public void updateRecordIndex_shouldNotUpdateCurrentKeysAndTotalRecords_whenKeyDoesNotExistInStagedDataOrDatabase() {
        recordIndex.updateRecordIndex(new Entry(1, new HashValue(HashingAlgorithm.SHA256, "de"), Instant.now(), "DE"));
        recordIndex.updateRecordIndex(new Entry(2, new HashValue(HashingAlgorithm.SHA256, "va"), Instant.now(), "VA"));
        recordIndex.checkpoint();

        assertThat(currentKeys.size(), is(2));
        assertThat(currentKeysUpdateDAO.getTotalRecords(), is(2));

        recordIndex.updateRecordIndex(new Entry(3, Collections.emptyList(), Instant.now(), "CZ"));
        recordIndex.checkpoint();

        assertThat(currentKeys.size(), is(3));
        assertThat(currentKeys.containsKey("CZ"), is(true));
        assertThat(currentKeysUpdateDAO.getTotalRecords(), is(2));
    }
}
