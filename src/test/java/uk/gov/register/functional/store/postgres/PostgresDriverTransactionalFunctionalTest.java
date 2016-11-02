package uk.gov.register.functional.store.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.skife.jdbi.v2.DBI;
import uk.gov.register.configuration.RegistersConfiguration;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.functional.app.WipeDatabaseRule;
import uk.gov.register.functional.db.TestDBSupport;
import uk.gov.register.store.postgres.PostgresDriverTransactional;
import uk.gov.verifiablelog.store.memoization.DoNothing;

import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;

public class PostgresDriverTransactionalFunctionalTest extends TestDBSupport {

    @Rule
    public TestRule wipe = new WipeDatabaseRule();

    @Test
    public void useTransactionShouldApplyChangesAtomicallyToDatabase() {
        DBI dbi = new DBI(postgresConnectionString);

        Item item1 = new Item("itemhash1", new ObjectMapper().createObjectNode());
        Item item2 = new Item("itemhash2", new ObjectMapper().createObjectNode());
        Item item3 = new Item("itemhash3", new ObjectMapper().createObjectNode());
        Entry entry1 = new Entry(1, "itemhash1", Instant.now());
        Entry entry2 = new Entry(2, "itemhash2", Instant.now());
        Entry entry3 = new Entry(3, "itemhash3", Instant.now());

        PostgresDriverTransactional.useTransaction(dbi, new DoNothing(), postgresDriver -> {
            postgresDriver.insertItem(item1);
            postgresDriver.insertEntry(entry1);

            assertThat(postgresDriver.getAllEntries().size(), is(1));
            assertThat(postgresDriver.getAllItems().size(), is(1));
            assertThat(testEntryDAO.getAllEntries(), is(empty()));
            assertThat(testItemDAO.getItems(), is(empty()));

            postgresDriver.insertItem(item2);
            postgresDriver.insertEntry(entry2);

            assertThat(postgresDriver.getAllEntries().size(), is(2));
            assertThat(postgresDriver.getAllItems().size(), is(2));
            assertThat(testEntryDAO.getAllEntries(), is(empty()));
            assertThat(testItemDAO.getItems(), is(empty()));

            postgresDriver.insertItem(item3);
            postgresDriver.insertEntry(entry3);

            assertThat(postgresDriver.getAllEntries().size(), is(3));
            assertThat(postgresDriver.getAllItems().size(), is(3));
            assertThat(testEntryDAO.getAllEntries(), is(empty()));
            assertThat(testItemDAO.getItems(), is(empty()));
        });

        assertThat(testEntryDAO.getAllEntries().size(), is(3));
        assertThat(testItemDAO.getItems().size(), is(3));
    }

    @Test
    public void useTransactionShouldRollbackIfExceptionThrown() {
        DBI dbi = new DBI(postgresConnectionString);

        Item item1 = new Item("itemhash1", new ObjectMapper().createObjectNode());
        Item item2 = new Item("itemhash2", new ObjectMapper().createObjectNode());
        Item item3 = new Item("itemhash3", new ObjectMapper().createObjectNode());
        Entry entry1 = new Entry(1, "itemhash1", Instant.now());
        Entry entry2 = new Entry(2, "itemhash2", Instant.now());
        Entry entry3 = new Entry(3, "itemhash3", Instant.now());

        try {
            PostgresDriverTransactional.useTransaction(dbi, new DoNothing(), postgresDriver -> {
                postgresDriver.insertItem(item1);
                postgresDriver.insertEntry(entry1);

                assertThat(postgresDriver.getAllEntries().size(), is(1));
                assertThat(postgresDriver.getAllItems().size(), is(1));
                assertThat(testEntryDAO.getAllEntries(), is(empty()));
                assertThat(testItemDAO.getItems(), is(empty()));

                postgresDriver.insertItem(item2);
                postgresDriver.insertEntry(entry2);

                assertThat(postgresDriver.getAllEntries().size(), is(2));
                assertThat(postgresDriver.getAllItems().size(), is(2));
                assertThat(testEntryDAO.getAllEntries(), is(empty()));
                assertThat(testItemDAO.getItems(), is(empty()));

                postgresDriver.insertItem(item3);
                postgresDriver.insertEntry(entry3);

                assertThat(postgresDriver.getAllEntries().size(), is(3));
                assertThat(postgresDriver.getAllItems().size(), is(3));
                assertThat(testEntryDAO.getAllEntries(), is(empty()));
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

