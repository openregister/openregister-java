package uk.gov.register.functional.store.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.skife.jdbi.v2.DBI;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.functional.app.WipeDatabaseRule;
import uk.gov.register.functional.db.TestDBSupport;
import uk.gov.register.store.postgres.PostgresDriverTransactional;
import uk.gov.register.util.HashValue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;

public class PostgresDriverTransactionalFunctionalTest extends TestDBSupport {

    @Rule
    public TestRule wipe = new WipeDatabaseRule();

    @Test
    public void useTransactionShouldApplyChangesAtomicallyToDatabase() {
        DBI dbi = new DBI(postgresConnectionString);

        Item item1 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash1"), new ObjectMapper().createObjectNode());
        Item item2 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash2"), new ObjectMapper().createObjectNode());
        Item item3 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash3"), new ObjectMapper().createObjectNode());

        PostgresDriverTransactional.useTransaction(dbi, postgresDriver -> {
            postgresDriver.insertItem(item1);

            assertThat(postgresDriver.getAllItems().size(), is(1));
            assertThat(testItemDAO.getItems(), is(empty()));

            postgresDriver.insertItem(item2);

            assertThat(postgresDriver.getAllItems().size(), is(2));
            assertThat(testItemDAO.getItems(), is(empty()));

            postgresDriver.insertItem(item3);

            assertThat(postgresDriver.getAllItems().size(), is(3));
            assertThat(testItemDAO.getItems(), is(empty()));
        });

        assertThat(testItemDAO.getItems().size(), is(3));
    }

    @Test
    public void useTransactionShouldRollbackIfExceptionThrown() {
        DBI dbi = new DBI(postgresConnectionString);

        Item item1 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash1"), new ObjectMapper().createObjectNode());
        Item item2 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash2"), new ObjectMapper().createObjectNode());
        Item item3 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash3"), new ObjectMapper().createObjectNode());

        try {
            PostgresDriverTransactional.useTransaction(dbi, postgresDriver -> {
                postgresDriver.insertItem(item1);

                assertThat(postgresDriver.getAllItems().size(), is(1));
                assertThat(testItemDAO.getItems(), is(empty()));

                postgresDriver.insertItem(item2);

                assertThat(postgresDriver.getAllItems().size(), is(2));
                assertThat(testItemDAO.getItems(), is(empty()));

                postgresDriver.insertItem(item3);

                assertThat(postgresDriver.getAllItems().size(), is(3));
                assertThat(testItemDAO.getItems(), is(empty()));

                throw new RuntimeException();
            });
        } catch (RuntimeException ex) {
            // intentionally thrown
        }

        assertThat(testEntryDAO.getAllEntries().size(), is(0));
        assertThat(testItemDAO.getItems().size(), is(0));
    }
}

