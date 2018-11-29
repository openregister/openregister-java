package uk.gov.register.functional.store.postgres;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import uk.gov.register.configuration.ConfigManager;
import uk.gov.register.configuration.FieldsConfiguration;
import uk.gov.register.configuration.RegistersConfiguration;
import uk.gov.register.core.*;
import uk.gov.register.db.*;
import uk.gov.register.db.RecordSet;
import uk.gov.register.functional.app.WipeDatabaseRule;
import uk.gov.register.functional.db.TestEntryDAO;
import uk.gov.register.functional.db.TestItemCommandDAO;
import uk.gov.register.service.EnvironmentValidator;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.store.postgres.BatchedPostgresDataAccessLayer;
import uk.gov.register.store.postgres.PostgresDataAccessLayer;
import uk.gov.register.util.HashValue;
import uk.gov.verifiablelog.store.memoization.DoNothing;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.register.functional.app.TestRegister.address;

public class RegisterImplTransactionalFunctionalTest {

    private static ObjectMapper MAPPER = new ObjectMapper();

    @Rule
    public TestRule wipe = new WipeDatabaseRule(address);
    private DBI dbi;
    private Handle handle;
    private String schema = address.getSchema();

    private TestItemCommandDAO testItemDAO;
    private TestEntryDAO testEntryDAO;
    private RecordSet recordSet;

    @Before
    public void setUp() throws Exception {
        final DBIFactory factory = new DBIFactory();
        Environment env = new Environment("test-env", Jackson.newObjectMapper(), null, new MetricRegistry(), null);
        dbi = factory.build(env, getDataSourceFactory(), "database");
        handle = dbi.open();
        testItemDAO = handle.attach(TestItemCommandDAO.class);
        testEntryDAO = handle.attach(TestEntryDAO.class);
        recordSet = mock(RecordSet.class);
    }

    @After
    public void tearDown() throws Exception {
        dbi.close(handle);
    }

    @Test
    public void getItemUsingBlobHash() {
        HashValue v1Hash = new HashValue(HashingAlgorithm.SHA256, "itemhash1");
        HashValue blobHash = new HashValue(HashingAlgorithm.SHA256, "itemhash2");
        Item item1 = new Item(v1Hash, blobHash, new ObjectMapper().createObjectNode());
        RegisterImpl registerImpl = getPostgresRegister(getDataAccessLayer(handle));
        registerImpl.addItem(item1);

        assertThat(registerImpl.getItem(blobHash), equalTo(Optional.of(item1)));
        assertThat(registerImpl.getItem(v1Hash), equalTo(Optional.empty()));
    }

    @Test
    public void getItemUsingV1Hash() {
        HashValue v1Hash = new HashValue(HashingAlgorithm.SHA256, "itemhash1");
        HashValue blobHash = new HashValue(HashingAlgorithm.SHA256, "itemhash2");
        Item item1 = new Item(v1Hash, blobHash, new ObjectMapper().createObjectNode());
        RegisterImpl registerImpl = getPostgresRegister(getDataAccessLayer(handle));
        registerImpl.addItem(item1);

        assertThat(registerImpl.getItemByV1Hash(v1Hash), equalTo(Optional.of(item1)));
        assertThat(registerImpl.getItemByV1Hash(blobHash), equalTo(Optional.empty()));
    }

    @Test
    public void useTransactionShouldApplyChangesAtomicallyToDatabase() {

        Item item1 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash1"), new ObjectMapper().createObjectNode());
        Item item2 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash2"), new ObjectMapper().createObjectNode());
        Item item3 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash3"), new ObjectMapper().createObjectNode());

        RegisterContext.useTransaction(dbi, handle -> {
            RegisterImpl registerImpl = getPostgresRegister(getTransactionalDataAccessLayer(handle));
            registerImpl.addItem(item1);

            assertThat(registerImpl.getAllItems().size(), is(1));
            assertThat(testItemDAO.getItems(schema), is(empty()));

            registerImpl.addItem(item2);

            assertThat(registerImpl.getAllItems().size(), is(2));
            assertThat(testItemDAO.getItems(schema), is(empty()));

            registerImpl.addItem(item3);

            assertThat(registerImpl.getAllItems().size(), is(3));
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
                BatchedPostgresDataAccessLayer dataAccessLayer = getTransactionalDataAccessLayer(handle);
                RegisterImpl registerImpl = getPostgresRegister(dataAccessLayer);
                registerImpl.addItem(item1);
                dataAccessLayer.writeBatchesToDatabase();

                assertThat(registerImpl.getAllItems().size(), is(1));
                assertThat(testItemDAO.getItems(schema), is(empty()));

                registerImpl.addItem(item2);
                dataAccessLayer.writeBatchesToDatabase();

                assertThat(registerImpl.getAllItems().size(), is(2));
                assertThat(testItemDAO.getItems(schema), is(empty()));

                registerImpl.addItem(item3);
                dataAccessLayer.writeBatchesToDatabase();

                assertThat(registerImpl.getAllItems().size(), is(3));
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
        JsonNode addressRegisterJson = MAPPER.readTree("{\"fields\":[\"address\"],\"phase\":\"alpha\",\"register\":\"address\",\"registry\":\"office-for-national-statistics\",\"text\":\"Register of addresses\"}");
        JsonNode addressFieldJson = MAPPER.readTree("{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"address\",\"phase\":\"alpha\",\"register\":\"address\",\"text\":\"A place in the UK with a postal address.\"}");

        Item addressField = new Item(new HashValue(HashingAlgorithm.SHA256, "cf5700d23d4cd933574fbafb48ba6ace1c3b374b931a6183eeefab6f37106011"), addressFieldJson);
        Item addressRegister = new Item(new HashValue(HashingAlgorithm.SHA256, "5e7a41b4d05ae4dfb910f3376453b21790c1ea439ef580d6dc63f067800cd9f1"), addressRegisterJson);
        Item item1 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash1"), new ObjectMapper().readTree("{\"address\":\"aaa\"}"));
        Item item2 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash2"), new ObjectMapper().readTree("{\"address\":\"bbb\"}"));
        Item item3 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash3"), new ObjectMapper().readTree("{\"address\":\"ccc\"}"));
        Entry addressFieldEntry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "cf5700d23d4cd933574fbafb48ba6ace1c3b374b931a6183eeefab6f37106011"), Instant.parse("2017-03-10T00:00:00Z"), "field:address", EntryType.system);
        Entry addressRegisterEntry = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "5e7a41b4d05ae4dfb910f3376453b21790c1ea439ef580d6dc63f067800cd9f1"), Instant.parse("2017-03-10T00:00:00Z"), "register:address", EntryType.system);
        Entry entry1 = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "itemhash1"), Instant.parse("2017-03-10T00:00:00Z"), "aaa", EntryType.user);
        Entry entry2 = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "itemhash2"), Instant.parse("2017-03-10T00:00:00Z"), "bbb", EntryType.user);
        Entry entry3 = new Entry(3, new HashValue(HashingAlgorithm.SHA256, "itemhash3"), Instant.parse("2017-03-10T00:00:00Z"), "ccc", EntryType.user);

        Record addressRegisterRecord = mock(Record.class);
        when(addressRegisterRecord.getItem()).thenReturn(addressRegister);
        when(recordSet.getRecord(EntryType.system, "register:address")).thenReturn(Optional.of(addressRegisterRecord));

        Record addressFieldRecord = mock(Record.class);
        when(addressFieldRecord.getItem()).thenReturn(addressField);
        when(recordSet.getRecord(EntryType.system, "field:address")).thenReturn(Optional.of(addressFieldRecord));

        RegisterContext.useTransaction(dbi, handle -> {
            BatchedPostgresDataAccessLayer dataAccessLayer = getTransactionalDataAccessLayer(handle);
            RegisterImpl registerImpl = getPostgresRegister(dataAccessLayer);
            registerImpl.addItem(addressField);
            registerImpl.addItem(addressRegister);
            registerImpl.addItem(item1);
            registerImpl.addItem(item2);
            registerImpl.addItem(item3);
            registerImpl.appendEntry(addressFieldEntry);
            registerImpl.appendEntry(addressRegisterEntry);
            registerImpl.appendEntry(entry1);
            registerImpl.appendEntry(entry2);
            registerImpl.appendEntry(entry3);
            dataAccessLayer.writeBatchesToDatabase();
        });

        List<HashValue> items = testItemDAO.getItems(schema).stream().map(item -> item.hashValue).collect(Collectors.toList());
        assertThat(items, containsInAnyOrder(addressField.getSha256hex(), addressRegister.getSha256hex(), item1.getSha256hex(), item2.getSha256hex(), item3.getSha256hex()));
        assertThat(testEntryDAO.getAllEntries(schema), contains(entry1, entry2, entry3));
    }

    private RegisterImpl getPostgresRegister(DataAccessLayer dataAccessLayer) {
        EntryLog entryLog = new EntryLogImpl(dataAccessLayer, new DoNothing());
        ItemValidator itemValidator = mock(ItemValidator.class);
        ItemStore itemStore = new ItemStoreImpl(dataAccessLayer);
        RegisterId registerId = new RegisterId("address");
        RegistersConfiguration registersConfiguration = mock(RegistersConfiguration.class);
        when(registersConfiguration.getRegisterMetadata(registerId)).thenReturn(new RegisterMetadata(registerId, Arrays.asList("address"), "copyright", "registry", "text", "phase"));
        FieldsConfiguration fieldsConfiguration = mock(FieldsConfiguration.class);
        when(fieldsConfiguration.getField("address")).thenReturn(Optional.of(new Field("address", "string", registerId, Cardinality.ONE, "A place in the UK with a postal address.")));
        
        ConfigManager configManager = mock(ConfigManager.class);
        when(configManager.getRegistersConfiguration()).thenReturn(registersConfiguration);
        when(configManager.getFieldsConfiguration()).thenReturn(fieldsConfiguration);
        RegisterMetadata registerData = mock(RegisterMetadata.class);
        when(registerData.getRegisterId()).thenReturn(new RegisterId("address"));

        EnvironmentValidator environmentValidator = mock(EnvironmentValidator.class);

        return new RegisterImpl(registerData.getRegisterId(),
                entryLog,
                itemStore,
                recordSet,
                itemValidator,
                environmentValidator);
    }

    private BatchedPostgresDataAccessLayer getTransactionalDataAccessLayer(Handle handle) {
        return new BatchedPostgresDataAccessLayer(getDataAccessLayer(handle));
    }

    private PostgresDataAccessLayer getDataAccessLayer(Handle handle) {
        return new PostgresDataAccessLayer(
                handle.attach(EntryDAO.class),
                handle.attach(EntryQueryDAO.class),
                handle.attach(ItemDAO.class),
                handle.attach(ItemQueryDAO.class),
                handle.attach(RecordQueryDAO.class),
                "address");
    }

    private DataSourceFactory getDataSourceFactory() {
        DataSourceFactory dataSourceFactory = new DataSourceFactory();
        dataSourceFactory.setDriverClass("org.postgresql.Driver");
        dataSourceFactory.setUrl("jdbc:postgresql://localhost:5432/ft_openregister_java_multi");
        dataSourceFactory.setUser("postgres");
        dataSourceFactory.setPassword("");
        return dataSourceFactory;
    }
}
