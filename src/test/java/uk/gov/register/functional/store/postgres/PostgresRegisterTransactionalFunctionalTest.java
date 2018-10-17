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
import uk.gov.register.configuration.IndexFunctionConfiguration.IndexNames;
import uk.gov.register.configuration.RegistersConfiguration;
import uk.gov.register.core.*;
import uk.gov.register.db.*;
import uk.gov.register.db.Index;
import uk.gov.register.functional.app.WipeDatabaseRule;
import uk.gov.register.functional.db.TestEntryDAO;
import uk.gov.register.functional.db.TestBlobCommandDAO;
import uk.gov.register.indexer.IndexDriver;
import uk.gov.register.indexer.function.IndexFunction;
import uk.gov.register.indexer.function.LatestByKeyIndexFunction;
import uk.gov.register.service.EnvironmentValidator;
import uk.gov.register.service.BlobValidator;
import uk.gov.register.store.DataAccessLayer;
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

public class PostgresRegisterTransactionalFunctionalTest {

    private static ObjectMapper MAPPER = new ObjectMapper();

    @Rule
    public TestRule wipe = new WipeDatabaseRule(address);
    private DBI dbi;
    private Handle handle;
    private String schema = address.getSchema();

    private TestBlobCommandDAO testItemDAO;
    private TestEntryDAO testEntryDAO;
    private IndexDriver indexDriver = mock(IndexDriver.class);
    private Index index;

    @Before
    public void setUp() throws Exception {
        final DBIFactory factory = new DBIFactory();
        Environment env = new Environment("test-env", Jackson.newObjectMapper(), null, new MetricRegistry(), null);
        dbi = factory.build(env, getDataSourceFactory(), "database");
        handle = dbi.open();
        testItemDAO = handle.attach(TestBlobCommandDAO.class);
        testEntryDAO = handle.attach(TestEntryDAO.class);
        index = mock(Index.class);
    }

    @After
    public void tearDown() throws Exception {
        dbi.close(handle);
    }

    @Test
    public void useTransactionShouldApplyChangesAtomicallyToDatabase() {

        Blob blob1 = new Blob(new HashValue(HashingAlgorithm.SHA256, "itemhash1"), new ObjectMapper().createObjectNode());
        Blob blob2 = new Blob(new HashValue(HashingAlgorithm.SHA256, "itemhash2"), new ObjectMapper().createObjectNode());
        Blob blob3 = new Blob(new HashValue(HashingAlgorithm.SHA256, "itemhash3"), new ObjectMapper().createObjectNode());

        RegisterContext.useTransaction(dbi, handle -> {
            PostgresRegister postgresRegister = getPostgresRegister(getTransactionalDataAccessLayer(handle));
            postgresRegister.addBlob(blob1);

            assertThat(postgresRegister.getAllBlobs().size(), is(1));
            assertThat(testItemDAO.getItems(schema), is(empty()));

            postgresRegister.addBlob(blob2);

            assertThat(postgresRegister.getAllBlobs().size(), is(2));
            assertThat(testItemDAO.getItems(schema), is(empty()));

            postgresRegister.addBlob(blob3);

            assertThat(postgresRegister.getAllBlobs().size(), is(3));
            assertThat(testItemDAO.getItems(schema), is(empty()));
        });

        assertThat(testItemDAO.getItems(schema).size(), is(3));
    }

    @Test
    public void useTransactionShouldRollbackIfExceptionThrown() {
        Blob blob1 = new Blob(new HashValue(HashingAlgorithm.SHA256, "itemhash1"), new ObjectMapper().createObjectNode());
        Blob blob2 = new Blob(new HashValue(HashingAlgorithm.SHA256, "itemhash2"), new ObjectMapper().createObjectNode());
        Blob blob3 = new Blob(new HashValue(HashingAlgorithm.SHA256, "itemhash3"), new ObjectMapper().createObjectNode());

        try {
            RegisterContext.useTransaction(dbi, handle -> {
                PostgresDataAccessLayer dataAccessLayer = getTransactionalDataAccessLayer(handle);
                PostgresRegister postgresRegister = getPostgresRegister(dataAccessLayer);
                postgresRegister.addBlob(blob1);
                dataAccessLayer.checkpoint();

                assertThat(postgresRegister.getAllBlobs().size(), is(1));
                assertThat(testItemDAO.getItems(schema), is(empty()));

                postgresRegister.addBlob(blob2);
                dataAccessLayer.checkpoint();

                assertThat(postgresRegister.getAllBlobs().size(), is(2));
                assertThat(testItemDAO.getItems(schema), is(empty()));

                postgresRegister.addBlob(blob3);
                dataAccessLayer.checkpoint();

                assertThat(postgresRegister.getAllBlobs().size(), is(3));
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

        Blob addressField = new Blob(new HashValue(HashingAlgorithm.SHA256, "cf5700d23d4cd933574fbafb48ba6ace1c3b374b931a6183eeefab6f37106011"), addressFieldJson);
        Blob addressRegister = new Blob(new HashValue(HashingAlgorithm.SHA256, "5e7a41b4d05ae4dfb910f3376453b21790c1ea439ef580d6dc63f067800cd9f1"), addressRegisterJson);
        Blob blob1 = new Blob(new HashValue(HashingAlgorithm.SHA256, "itemhash1"), new ObjectMapper().readTree("{\"address\":\"aaa\"}"));
        Blob blob2 = new Blob(new HashValue(HashingAlgorithm.SHA256, "itemhash2"), new ObjectMapper().readTree("{\"address\":\"bbb\"}"));
        Blob blob3 = new Blob(new HashValue(HashingAlgorithm.SHA256, "itemhash3"), new ObjectMapper().readTree("{\"address\":\"ccc\"}"));
        BaseEntry addressFieldEntry = new BaseEntry(1, new HashValue(HashingAlgorithm.SHA256, "cf5700d23d4cd933574fbafb48ba6ace1c3b374b931a6183eeefab6f37106011"), Instant.parse("2017-03-10T00:00:00Z"), "field:address", EntryType.system);
        BaseEntry addressRegisterEntry = new BaseEntry(2, new HashValue(HashingAlgorithm.SHA256, "5e7a41b4d05ae4dfb910f3376453b21790c1ea439ef580d6dc63f067800cd9f1"), Instant.parse("2017-03-10T00:00:00Z"), "register:address", EntryType.system);
        BaseEntry entry1 = new BaseEntry(3, new HashValue(HashingAlgorithm.SHA256, "itemhash1"), Instant.parse("2017-03-10T00:00:00Z"), "aaa", EntryType.user);
        BaseEntry entry2 = new BaseEntry(4, new HashValue(HashingAlgorithm.SHA256, "itemhash2"), Instant.parse("2017-03-10T00:00:00Z"), "bbb", EntryType.user);
        BaseEntry entry3 = new BaseEntry(5, new HashValue(HashingAlgorithm.SHA256, "itemhash3"), Instant.parse("2017-03-10T00:00:00Z"), "ccc", EntryType.user);

        Record addressRegisterRecord = mock(Record.class);
        when(addressRegisterRecord.getBlobs()).thenReturn(Arrays.asList(addressRegister));
        when(index.getRecord("register:address", IndexNames.METADATA)).thenReturn(Optional.of(addressRegisterRecord));

        Record addressFieldRecord = mock(Record.class);
        when(addressFieldRecord.getBlobs()).thenReturn(Arrays.asList(addressField));
        when(index.getRecord("field:address", IndexNames.METADATA)).thenReturn(Optional.of(addressFieldRecord));

        RegisterContext.useTransaction(dbi, handle -> {
            PostgresDataAccessLayer dataAccessLayer = getTransactionalDataAccessLayer(handle);
            PostgresRegister postgresRegister = getPostgresRegister(dataAccessLayer);
            postgresRegister.addBlob(addressField);
            postgresRegister.addBlob(addressRegister);
            postgresRegister.addBlob(blob1);
            postgresRegister.addBlob(blob2);
            postgresRegister.addBlob(blob3);
            postgresRegister.appendEntry(addressFieldEntry);
            postgresRegister.appendEntry(addressRegisterEntry);
            postgresRegister.appendEntry(entry1);
            postgresRegister.appendEntry(entry2);
            postgresRegister.appendEntry(entry3);
            dataAccessLayer.checkpoint();
        });

        List<HashValue> items = testItemDAO.getItems(schema).stream().map(item -> item.hashValue).collect(Collectors.toList());
        assertThat(items, containsInAnyOrder(addressField.getSha256hex(), addressRegister.getSha256hex(), blob1.getSha256hex(), blob2.getSha256hex(), blob3.getSha256hex()));
        assertThat(testEntryDAO.getAllEntries(schema), contains(entry1, entry2, entry3));
    }

    private PostgresRegister getPostgresRegister(DataAccessLayer dataAccessLayer) {
        EntryLog entryLog = new EntryLogImpl(dataAccessLayer, new DoNothing());
        BlobValidator blobValidator = mock(BlobValidator.class);
        BlobStore blobStore = new BlobStoreImpl(dataAccessLayer);
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

        return new PostgresRegister(registerData.getRegisterId(),
                entryLog,
                blobStore,
                index,
                getIndexFunctions(),
                blobValidator,
                environmentValidator);
    }

    private PostgresDataAccessLayer getTransactionalDataAccessLayer(Handle handle) {
        return new PostgresDataAccessLayer(
                handle.attach(EntryQueryDAO.class),
                handle.attach(IndexDAO.class),
                handle.attach(IndexQueryDAO.class),
                handle.attach(EntryDAO.class),
                handle.attach(EntryBlobDAO.class),
                handle.attach(BlobQueryDAO.class),
                handle.attach(BlobDAO.class),
                "address",
                indexDriver,
                ImmutableMap.of(EntryType.user, Arrays.asList(new LatestByKeyIndexFunction(IndexNames.RECORD)), EntryType.system, Arrays.asList(new LatestByKeyIndexFunction(IndexNames.METADATA))));
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

