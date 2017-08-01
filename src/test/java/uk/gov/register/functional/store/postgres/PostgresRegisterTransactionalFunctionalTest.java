package uk.gov.register.functional.store.postgres;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
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
import uk.gov.register.functional.app.WipeDatabaseRule;
import uk.gov.register.functional.db.TestEntryDAO;
import uk.gov.register.functional.db.TestItemCommandDAO;
import uk.gov.register.indexer.IndexDriver;
import uk.gov.register.indexer.function.LatestByKeyIndexFunction;
import uk.gov.register.indexer.function.IndexFunction;
import uk.gov.register.service.EnvironmentValidator;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.store.postgres.PostgresDataAccessLayer;
import uk.gov.register.util.HashValue;
import uk.gov.verifiablelog.store.memoization.DoNothing;
import uk.gov.register.configuration.IndexFunctionConfiguration.IndexNames;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.register.functional.app.TestRegister.address;

public class PostgresRegisterTransactionalFunctionalTest {

    private static ObjectMapper MAPPER = new ObjectMapper();

    @Rule
    public TestRule wipe = new WipeDatabaseRule(address);
    private DBI dbi;
    private Handle handle;
    private String schema = address.getSchema();

    private TestItemCommandDAO testItemDAO;
    private TestEntryDAO testEntryDAO;
    private IndexDriver indexDriver = mock(IndexDriver.class);
    private DerivationRecordIndex derivationRecordIndex;

    @Before
    public void setUp() throws Exception {
        final DBIFactory factory = new DBIFactory();
        Environment env = new Environment("test-env", Jackson.newObjectMapper(), null, new MetricRegistry(), null);
        dbi = factory.build(env, getDataSourceFactory(), "database");
        handle = dbi.open();
        testItemDAO = handle.attach(TestItemCommandDAO.class);
        testEntryDAO = handle.attach(TestEntryDAO.class);
        derivationRecordIndex = mock(DerivationRecordIndex.class);
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
        JsonNode addressRegisterJson = MAPPER.readTree("{\"fields\":[\"address\"],\"phase\":\"alpha\",\"register\":\"address\",\"registry\":\"office-for-national-statistics\",\"text\":\"Register of addresses\"}");
        JsonNode addressFieldJson = MAPPER.readTree("{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"address\",\"phase\":\"alpha\",\"register\":\"address\",\"text\":\"A place in the UK with a postal address.\"}");

        Item addressField = new Item(new HashValue(HashingAlgorithm.SHA256, "cf5700d23d4cd933574fbafb48ba6ace1c3b374b931a6183eeefab6f37106011"), addressFieldJson);
        Item addressRegister = new Item(new HashValue(HashingAlgorithm.SHA256, "5e7a41b4d05ae4dfb910f3376453b21790c1ea439ef580d6dc63f067800cd9f1"), addressRegisterJson);
        Item item1 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash1"), new ObjectMapper().readTree("{\"address\":\"aaa\"}"));
        Item item2 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash2"), new ObjectMapper().readTree("{\"address\":\"bbb\"}"));
        Item item3 = new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash3"), new ObjectMapper().readTree("{\"address\":\"ccc\"}"));
        Entry addressFieldEntry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "cf5700d23d4cd933574fbafb48ba6ace1c3b374b931a6183eeefab6f37106011"), Instant.parse("2017-03-10T00:00:00Z"), "field:address", EntryType.system);
        Entry addressRegisterEntry = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "5e7a41b4d05ae4dfb910f3376453b21790c1ea439ef580d6dc63f067800cd9f1"), Instant.parse("2017-03-10T00:00:00Z"), "register:address", EntryType.system);
        Entry entry1 = new Entry(3, new HashValue(HashingAlgorithm.SHA256, "itemhash1"), Instant.parse("2017-03-10T00:00:00Z"), "aaa", EntryType.user);
        Entry entry2 = new Entry(4, new HashValue(HashingAlgorithm.SHA256, "itemhash2"), Instant.parse("2017-03-10T00:00:00Z"), "bbb", EntryType.user);
        Entry entry3 = new Entry(5, new HashValue(HashingAlgorithm.SHA256, "itemhash3"), Instant.parse("2017-03-10T00:00:00Z"), "ccc", EntryType.user);

        Record addressRegisterRecord = mock(Record.class);
        when(addressRegisterRecord.getItems()).thenReturn(Arrays.asList(addressRegister));
        when(derivationRecordIndex.getRecord("register:address", IndexNames.METADATA)).thenReturn(Optional.of(addressRegisterRecord));

        Record addressFieldRecord = mock(Record.class);
        when(addressFieldRecord.getItems()).thenReturn(Arrays.asList(addressField));
        when(derivationRecordIndex.getRecord("field:address", IndexNames.METADATA)).thenReturn(Optional.of(addressFieldRecord));

        RegisterContext.useTransaction(dbi, handle -> {
            PostgresDataAccessLayer dataAccessLayer = getTransactionalDataAccessLayer(handle);
            PostgresRegister postgresRegister = getPostgresRegister(dataAccessLayer);
            postgresRegister.putItem(addressField);
            postgresRegister.putItem(addressRegister);
            postgresRegister.putItem(item1);
            postgresRegister.putItem(item2);
            postgresRegister.putItem(item3);
            postgresRegister.appendEntry(addressFieldEntry);
            postgresRegister.appendEntry(addressRegisterEntry);
            postgresRegister.appendEntry(entry1);
            postgresRegister.appendEntry(entry2);
            postgresRegister.appendEntry(entry3);
            dataAccessLayer.checkpoint();
        });

        List<HashValue> items = testItemDAO.getItems(schema).stream().map(item -> item.hashValue).collect(Collectors.toList());
        assertThat(items, containsInAnyOrder(addressField.getSha256hex(), addressRegister.getSha256hex(), item1.getSha256hex(), item2.getSha256hex(), item3.getSha256hex()));
        assertThat(testEntryDAO.getAllEntries(schema), contains(entry1, entry2, entry3));
    }

    private PostgresRegister getPostgresRegister(DataAccessLayer dataAccessLayer) {
        EntryLog entryLog = new EntryLogImpl(dataAccessLayer, new DoNothing());
        ItemValidator itemValidator = mock(ItemValidator.class);
        ItemStore itemStore = new ItemStoreImpl(dataAccessLayer);
        RegisterName registerName = new RegisterName("address");
        RegistersConfiguration registersConfiguration = mock(RegistersConfiguration.class);
        when(registersConfiguration.getRegisterMetadata(registerName)).thenReturn(new RegisterMetadata(registerName, Arrays.asList("address"), "copyright", "registry", "text", "phase"));
        FieldsConfiguration fieldsConfiguration = mock(FieldsConfiguration.class);
        when(fieldsConfiguration.getField("address")).thenReturn(Optional.of(new Field("address", "string", registerName, Cardinality.ONE, "A place in the UK with a postal address.")));
        
        ConfigManager configManager = mock(ConfigManager.class);
        when(configManager.getRegistersConfiguration()).thenReturn(registersConfiguration);
        when(configManager.getFieldsConfiguration()).thenReturn(fieldsConfiguration);
        RegisterMetadata registerData = mock(RegisterMetadata.class);
        when(registerData.getRegisterName()).thenReturn(new RegisterName("address"));

        EnvironmentValidator environmentValidator = mock(EnvironmentValidator.class);

        return new PostgresRegister(registerData.getRegisterName(),
                entryLog,
                itemStore,
                new RecordIndexImpl(dataAccessLayer),
                derivationRecordIndex,
                getIndexFunctions(),
                indexDriver,
                itemValidator,
                environmentValidator);
    }

    private PostgresDataAccessLayer getTransactionalDataAccessLayer(Handle handle) {
        return new PostgresDataAccessLayer(
                handle.attach(EntryQueryDAO.class),
                handle.attach(IndexDAO.class),
                handle.attach(IndexQueryDAO.class),
                handle.attach(EntryDAO.class),
                handle.attach(EntryItemDAO.class),
                handle.attach(ItemQueryDAO.class),
                handle.attach(ItemDAO.class),
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

    private Map<EntryType, Collection<IndexFunction>> getIndexFunctions() {
        return ImmutableMap.of(EntryType.user, Collections.emptyList(), EntryType.system, Arrays.asList(new LatestByKeyIndexFunction(IndexNames.METADATA)));
    }
}

