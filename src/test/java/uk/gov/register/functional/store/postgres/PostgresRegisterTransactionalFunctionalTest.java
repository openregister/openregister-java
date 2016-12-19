package uk.gov.register.functional.store.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jdbi.OptionalContainerFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import uk.gov.register.configuration.RegisterFieldsConfiguration;
import uk.gov.register.core.*;
import uk.gov.register.db.*;
import uk.gov.register.functional.app.WipeDatabaseRule;
import uk.gov.register.functional.db.TestDBSupport;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.util.HashValue;
import uk.gov.verifiablelog.store.memoization.DoNothing;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PostgresRegisterTransactionalFunctionalTest extends TestDBSupport {

    @Rule
    public TestRule wipe = new WipeDatabaseRule();

    @Test
    public void useTransactionShouldApplyChangesAtomicallyToDatabase() {
        DBI dbi = new DBI(postgresConnectionString);

        Item item1 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash1"), new ObjectMapper().createObjectNode());
        Item item2 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash2"), new ObjectMapper().createObjectNode());
        Item item3 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash3"), new ObjectMapper().createObjectNode());

        RegisterContext.useTransaction(dbi, handle -> {
            PostgresRegister postgresRegister = getPostgresRegister(handle);
            postgresRegister.putItem(item1);

            assertThat(postgresRegister.getAllItems().size(), is(1));
            assertThat(testItemDAO.getItems(), is(empty()));

            postgresRegister.putItem(item2);

            assertThat(postgresRegister.getAllItems().size(), is(2));
            assertThat(testItemDAO.getItems(), is(empty()));

            postgresRegister.putItem(item3);

            assertThat(postgresRegister.getAllItems().size(), is(3));
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
            RegisterContext.useTransaction(dbi, handle -> {
                PostgresRegister postgresRegister = getPostgresRegister(handle);
                postgresRegister.putItem(item1);
                postgresRegister.commit();

                assertThat(postgresRegister.getAllItems().size(), is(1));
                assertThat(testItemDAO.getItems(), is(empty()));

                postgresRegister.putItem(item2);
                postgresRegister.commit();

                assertThat(postgresRegister.getAllItems().size(), is(2));
                assertThat(testItemDAO.getItems(), is(empty()));

                postgresRegister.putItem(item3);
                postgresRegister.commit();

                assertThat(postgresRegister.getAllItems().size(), is(3));
                assertThat(testItemDAO.getItems(), is(empty()));

                throw new RuntimeException();
            });
        } catch (RuntimeException ignored) {
            // intentionally thrown
        }

        assertThat(testEntryDAO.getAllEntries().size(), is(0));
        assertThat(testItemDAO.getItems().size(), is(0));
    }

    @Test
    public void entryAndItemDataShouldBeCommittedInOrder() throws Exception {
        DBI dbi = new DBI(postgresConnectionString);
        dbi.registerContainerFactory(new OptionalContainerFactory());

        Item item1 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash1"), new ObjectMapper().readTree("{\"address\":\"aaa\"}"));
        Item item2 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash2"), new ObjectMapper().readTree("{\"address\":\"bbb\"}"));
        Item item3 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash3"), new ObjectMapper().readTree("{\"address\":\"ccc\"}"));
        Entry entry1 = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "itemhash1"), Instant.now(), "aaa");
        Entry entry2 = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "itemhash2"), Instant.now(), "bbb");
        Entry entry3 = new Entry(3, new HashValue(HashingAlgorithm.SHA256, "itemhash3"), Instant.now(), "ccc");

        RegisterContext.useTransaction(dbi, handle -> {
            PostgresRegister postgresRegister = getPostgresRegister(handle);
            postgresRegister.putItem(item1);
            postgresRegister.putItem(item2);
            postgresRegister.putItem(item3);
            postgresRegister.appendEntry(entry1);
            postgresRegister.appendEntry(entry2);
            postgresRegister.appendEntry(entry3);
            postgresRegister.commit();
        });

        List<HashValue> items = testItemDAO.getItems().stream().map(item -> item.hashValue).collect(Collectors.toList());
        assertThat(items, containsInAnyOrder(item1.getSha256hex(), item2.getSha256hex(), item3.getSha256hex()));
        assertThat(testEntryDAO.getAllEntries(), contains(entry1, entry2, entry3));
    }

    public PostgresRegister getPostgresRegister(Handle handle) {
        TransactionalEntryLog entryLog = new TransactionalEntryLog(new DoNothing(), handle.attach(EntryQueryDAO.class), handle.attach(EntryDAO.class));
        TransactionalItemStore itemStore = new TransactionalItemStore(handle.attach(ItemDAO.class), handle.attach(ItemQueryDAO.class),
                mock(ItemValidator.class));
        RegisterMetadata registerData = mock(RegisterMetadata.class);
        when(registerData.getRegisterName()).thenReturn(new RegisterName("address"));
        return new PostgresRegister(registerData, new RegisterFieldsConfiguration(emptyList()), entryLog, itemStore, new TransactionalRecordIndex(handle.attach(RecordQueryDAO.class), handle.attach(CurrentKeysUpdateDAO.class)));
    }
}

