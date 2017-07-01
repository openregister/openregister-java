package uk.gov.register.functional.store.postgres;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.register.functional.app.TestRegister.address;

import uk.gov.register.configuration.RegisterFieldsConfiguration;
import uk.gov.register.core.*;
import uk.gov.register.db.*;
import uk.gov.register.functional.app.WipeDatabaseRule;
import uk.gov.register.functional.db.TestEntryDAO;
import uk.gov.register.functional.db.TestItemCommandDAO;
import uk.gov.register.indexer.IndexDriver;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.store.postgres.PostgresDataAccessLayer;
import uk.gov.register.util.HashValue;
import uk.gov.verifiablelog.store.memoization.DoNothing;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jdbi.OptionalContainerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

public class PostgresRegisterTransactionalFunctionalTest {

    @Rule
    public TestRule wipe = new WipeDatabaseRule(address);
    private DBI dbi;
    private Handle handle;
    private String schema = address.getSchema();

    private TestItemCommandDAO testItemDAO;
    private TestEntryDAO testEntryDAO;
    private DerivationRecordIndex derivationRecordIndex = mock(DerivationRecordIndex.class);
    private IndexDriver indexDriver = mock(IndexDriver.class);

    @Before
    public void setUp() throws Exception {
        dbi = new DBI(address.getDatabaseConnectionString("PGRegisterTxnFT"));
        dbi.registerContainerFactory(new OptionalContainerFactory());
        handle = dbi.open();
        testItemDAO = handle.attach(TestItemCommandDAO.class);
        testEntryDAO = handle.attach(TestEntryDAO.class);
    }

    @After
    public void tearDown() throws Exception {
        dbi.close(handle);
    }

    @Test
    public void useTransactionShouldApplyChangesAtomicallyToDatabase() {

        Item item1 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash1"), new ObjectMapper().createObjectNode());
        Item item2 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash2"), new ObjectMapper().createObjectNode());
        Item item3 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash3"), new ObjectMapper().createObjectNode());

        RegisterContext.useTransaction(dbi, handle -> {
            PostgresRegister postgresRegister = getPostgresRegister(getTransactionalDataAccessLayer(handle));
            postgresRegister.putItem(item1);

            assertThat(postgresRegister.getAllItems().size(), is(1));
            assertThat(testItemDAO.getItems(schema), is(empty()));

            postgresRegister.putItem(item2);

            assertThat(postgresRegister.getAllItems().size(), is(2));
            assertThat(testItemDAO.getItems(schema), is(empty()));

            postgresRegister.putItem(item3);

            assertThat(postgresRegister.getAllItems().size(), is(3));
            assertThat(testItemDAO.getItems(schema), is(empty()));
        });

        assertThat(testItemDAO.getItems(schema).size(), is(3));
    }

    @Test
    public void useTransactionShouldRollbackIfExceptionThrown() {
        Item item1 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash1"), new ObjectMapper().createObjectNode());
        Item item2 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash2"), new ObjectMapper().createObjectNode());
        Item item3 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash3"), new ObjectMapper().createObjectNode());

        try {
            RegisterContext.useTransaction(dbi, handle -> {
                PostgresDataAccessLayer dataAccessLayer = getTransactionalDataAccessLayer(handle);
                PostgresRegister postgresRegister = getPostgresRegister(dataAccessLayer);
                postgresRegister.putItem(item1);
                dataAccessLayer.checkpoint();

                assertThat(postgresRegister.getAllItems().size(), is(1));
                assertThat(testItemDAO.getItems(schema), is(empty()));

                postgresRegister.putItem(item2);
                dataAccessLayer.checkpoint();

                assertThat(postgresRegister.getAllItems().size(), is(2));
                assertThat(testItemDAO.getItems(schema), is(empty()));

                postgresRegister.putItem(item3);
                dataAccessLayer.checkpoint();

                assertThat(postgresRegister.getAllItems().size(), is(3));
                assertThat(testItemDAO.getItems(schema), is(empty()));

                throw new RuntimeException();
            });
        } catch (RuntimeException ignored) {
            // intentionally thrown
        }

        assertThat(testEntryDAO.getAllEntries(schema).size(), is(0));
        assertThat(testItemDAO.getItems(schema).size(), is(0));
    }

    @Test
    public void entryAndItemDataShouldBeCommittedInOrder() throws Exception {
        Item item1 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash1"), new ObjectMapper().readTree("{\"address\":\"aaa\"}"));
        Item item2 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash2"), new ObjectMapper().readTree("{\"address\":\"bbb\"}"));
        Item item3 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash3"), new ObjectMapper().readTree("{\"address\":\"ccc\"}"));
        Entry entry1 = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "itemhash1"), Instant.parse("2017-03-10T00:00:00Z"), "aaa", EntryType.user);
        Entry entry2 = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "itemhash2"), Instant.parse("2017-03-10T00:00:00Z"), "bbb", EntryType.user);
        Entry entry3 = new Entry(3, new HashValue(HashingAlgorithm.SHA256, "itemhash3"), Instant.parse("2017-03-10T00:00:00Z"), "ccc", EntryType.user);

        RegisterContext.useTransaction(dbi, handle -> {
            PostgresDataAccessLayer dataAccessLayer = getTransactionalDataAccessLayer(handle);
            PostgresRegister postgresRegister = getPostgresRegister(dataAccessLayer);
            postgresRegister.putItem(item1);
            postgresRegister.putItem(item2);
            postgresRegister.putItem(item3);
            postgresRegister.appendEntry(entry1);
            postgresRegister.appendEntry(entry2);
            postgresRegister.appendEntry(entry3);
            dataAccessLayer.checkpoint();
        });

        List<HashValue> items = testItemDAO.getItems(schema).stream().map(item -> item.hashValue).collect(Collectors.toList());
        assertThat(items, containsInAnyOrder(item1.getSha256hex(), item2.getSha256hex(), item3.getSha256hex()));
        assertThat(testEntryDAO.getAllEntries(schema), contains(entry1, entry2, entry3));
    }

    private PostgresRegister getPostgresRegister(DataAccessLayer dataAccessLayer) {
        EntryLog entryLog = new EntryLogImpl(dataAccessLayer, new DoNothing());
        ItemStore itemStore = new ItemStoreImpl(dataAccessLayer, mock(ItemValidator.class));
        RegisterMetadata registerData = mock(RegisterMetadata.class);
        when(registerData.getRegisterName()).thenReturn(new RegisterName("address"));
        return new PostgresRegister(registerData, new RegisterFieldsConfiguration(emptyList()), entryLog, itemStore,
                new RecordIndexImpl(dataAccessLayer),
                derivationRecordIndex, Collections.emptyList(), indexDriver);
    }

    private PostgresDataAccessLayer getTransactionalDataAccessLayer(Handle handle) {
        return new PostgresDataAccessLayer(
                handle.attach(EntryQueryDAO.class),
                handle.attach(IndexQueryDAO.class),
                handle.attach(EntryDAO.class),
                handle.attach(EntryItemDAO.class),
                handle.attach(ItemQueryDAO.class),
                handle.attach(ItemDAO.class),
                handle.attach(RecordQueryDAO.class),
                handle.attach(IndexDAO.class),
                "address");
    }
}

