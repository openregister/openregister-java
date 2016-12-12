package uk.gov.register.db;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

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
        recordIndex.updateRecordIndex("foo", 5);

        assertThat(currentKeys.entrySet(), is(empty()));
    }

    // TODO: more tests

    @Test
    public void insertRecordWithSameKeyValueDoesNotStageBothCurrentKeys() {
        recordIndex.updateRecordIndex("DE", 1);
        recordIndex.updateRecordIndex("VA", 2);
        recordIndex.updateRecordIndex("DE", 3);

        assertThat(currentKeys.entrySet(), is(empty()));

        recordIndex.checkpoint(); // force writing staged data

        assertThat(currentKeys.size(), is(2));
        assertThat(currentKeys.get("DE"), is(3));
        assertThat(currentKeys.get("VA"), is(2));
    }

    @Test
    public void whenInserting_shouldUpdateRecordCount() throws Exception {
        recordIndex.updateRecordIndex("DE", 1);
        recordIndex.updateRecordIndex("VA", 2);
        recordIndex.updateRecordIndex("DE", 3);
        recordIndex.checkpoint(); // force writing staged data

        assertThat(currentKeys.size(), is(2));
        assertThat(currentKeysUpdateDAO.getTotalRecords(), is(2));
        recordIndex.updateRecordIndex("CZ", 4);
        recordIndex.updateRecordIndex("TV", 5);
        recordIndex.checkpoint();

        assertThat(currentKeys.size(), is(4));
        assertThat(currentKeysUpdateDAO.getTotalRecords(), is(4));
    }
}
