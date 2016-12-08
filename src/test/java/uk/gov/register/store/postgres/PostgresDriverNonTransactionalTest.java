package uk.gov.register.store.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.util.HashValue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;

public class PostgresDriverNonTransactionalTest extends PostgresDriverTestBase {

    @Test
    public void insertItemShouldCommitItemImmediatelyInOrder() throws Exception {
        PostgresDriverNonTransactional postgresDriver = new PostgresDriverNonTransactional(
                dbi, memoizationStore, h -> itemQueryDAO, h -> itemDAO, h -> recordQueryDAO, h -> currentKeysUpdateDAO);

        Item item1 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash1"), new ObjectMapper().createObjectNode());
        Item item2 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash2"), new ObjectMapper().createObjectNode());
        Item item3 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash3"), new ObjectMapper().createObjectNode());
        Item item4 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash4"), new ObjectMapper().createObjectNode());

        postgresDriver.insertItem(item1);
        postgresDriver.insertItem(item2);
        assertThat(items.size(), is(2));
        assertThat(items, contains(item1, item2));

        postgresDriver.insertItem(item3);
        postgresDriver.insertItem(item4);
        assertThat(items.size(), is(4));
        assertThat(items, contains(item1, item2, item3, item4));
    }

    @Test
    public void insertRecordShouldCommitRecordImmediatelyInOrder() throws Exception {
        PostgresDriverNonTransactional postgresDriver = new PostgresDriverNonTransactional(
                dbi, memoizationStore, h -> itemQueryDAO, h -> itemDAO, h -> recordQueryDAO, h -> currentKeysUpdateDAO);

        postgresDriver.insertRecord(mockRecord("country", "DE", 1), "country");
        assertThat(currentKeys.size(), is(1));
        assertThat(currentKeys.get(0).getKey(), is("DE"));
        assertThat(currentKeys.get(0).getEntry_number(), is(1));

        postgresDriver.insertRecord(mockRecord("country", "VA", 2), "country");
        postgresDriver.insertRecord(mockRecord("country", "DE", 3), "country");
        assertThat(currentKeys.size(), is(2));
        assertThat(currentKeys.get(0).getKey(), is("VA"));
        assertThat(currentKeys.get(0).getEntry_number(), is(2));
        assertThat(currentKeys.get(1).getKey(), is("DE"));
        assertThat(currentKeys.get(1).getEntry_number(), is(3));
    }
}
