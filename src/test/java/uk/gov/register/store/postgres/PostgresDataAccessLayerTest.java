package uk.gov.register.store.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import io.dropwizard.jdbi.OptionalContainerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.db.EntryDAO;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.db.ItemDAO;
import uk.gov.register.db.ItemQueryDAO;
import uk.gov.register.db.RecordQueryDAO;
import uk.gov.register.functional.app.TestRegister;
import uk.gov.register.functional.app.WipeDatabaseRule;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.register.core.HashingAlgorithm.*;

public class PostgresDataAccessLayerTest {
    private PostgresDataAccessLayer postgresDataAccessLayer;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Rule
    public WipeDatabaseRule wipeDatabaseRule = new WipeDatabaseRule(TestRegister.address);
    private DBI dbi;
    private Handle handle;

    @Before
    public void setup() {
        TestRegister register = TestRegister.address;
        dbi = new DBI(register.getDatabaseConnectionString("BatchedPostgresDataAccessLayerTest"));
        dbi.registerContainerFactory(new OptionalContainerFactory());
        handle = dbi.open();

        postgresDataAccessLayer = new PostgresDataAccessLayer(
                handle.attach(EntryDAO.class),
                handle.attach(EntryQueryDAO.class),
                handle.attach(ItemDAO.class),
                handle.attach(ItemQueryDAO.class),
                handle.attach(RecordQueryDAO.class),
                register.getSchema());
    }

    @After
    public void tearDown() {
        dbi.close(handle);
    }

    @Test
    public void canAppendAndGetEntry() {
        Instant timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Entry entry1 = new Entry(1, new HashValue(SHA256, "itemhash1"), timestamp, "key1", EntryType.user);
        Entry entry2 = new Entry(2, new HashValue(SHA256, "itemhash2"), timestamp, "key2", EntryType.user);

        postgresDataAccessLayer.appendEntry(entry1);
        postgresDataAccessLayer.appendEntry(entry2);
        assertThat(postgresDataAccessLayer.getEntry(1), is(Optional.of(entry1)));
        assertThat(postgresDataAccessLayer.getEntry(2), is(Optional.of(entry2)));
        assertThat(postgresDataAccessLayer.getEntry(3), is(Optional.empty()));
    }

    @Test
    public void canAppendEntriesInBatch() {
        Instant timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Entry entry1 = new Entry(1, new HashValue(SHA256, "itemhash1"), timestamp, "key1", EntryType.user);
        Entry entry2 = new Entry(2, new HashValue(SHA256, "itemhash2"), timestamp, "key2", EntryType.user);

        postgresDataAccessLayer.appendEntries(Arrays.asList(entry1, entry2));
        assertThat(postgresDataAccessLayer.getEntry(1), is(Optional.of(entry1)));
        assertThat(postgresDataAccessLayer.getEntry(2), is(Optional.of(entry2)));
        assertThat(postgresDataAccessLayer.getEntry(3), is(Optional.empty()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void appendUserEntriesThatSkipEntryNumberThrowsException() {
        Instant timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Entry entry1 = new Entry(1, new HashValue(SHA256, "itemhash1"), timestamp, "key1", EntryType.user);
        Entry entry2 = new Entry(3, new HashValue(SHA256, "itemhash2"), timestamp, "key2", EntryType.user);

        postgresDataAccessLayer.appendEntry(entry1);
        postgresDataAccessLayer.appendEntry(entry2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void appendUserEntriesWithDuplicateEntryNumberThrowsException() {
        Instant timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Entry entry1 = new Entry(1, new HashValue(SHA256, "itemhash1"), timestamp, "key1", EntryType.user);
        Entry entry2 = new Entry(1, new HashValue(SHA256, "itemhash2"), timestamp, "key2", EntryType.user);

        postgresDataAccessLayer.appendEntry(entry1);
        postgresDataAccessLayer.appendEntry(entry2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void appendSystemEntriesThatSkipEntryNumberThrowsException() {
        Instant timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Entry entry1 = new Entry(1, new HashValue(SHA256, "itemhash1"), timestamp, "key1", EntryType.system);
        Entry entry2 = new Entry(3, new HashValue(SHA256, "itemhash2"), timestamp, "key2", EntryType.system);

        postgresDataAccessLayer.appendEntry(entry1);
        postgresDataAccessLayer.appendEntry(entry2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void appendSystemEntriesWithDuplicateEntryNumberThrowsException() {
        Instant timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Entry entry1 = new Entry(1, new HashValue(SHA256, "itemhash1"), timestamp, "key1", EntryType.system);
        Entry entry2 = new Entry(1, new HashValue(SHA256, "itemhash2"), timestamp, "key2", EntryType.system);

        postgresDataAccessLayer.appendEntry(entry1);
        postgresDataAccessLayer.appendEntry(entry2);
    }

    @Test
    public void canGetMultipleEntries() {
        Instant timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Entry entry1 = new Entry(1, new HashValue(SHA256, "itemhash1"), timestamp, "key1", EntryType.user);
        Entry entry2 = new Entry(2, new HashValue(SHA256, "itemhash2"), timestamp, "key2", EntryType.user);
        Entry entry3 = new Entry(3, new HashValue(SHA256, "itemhash3"), timestamp, "key3", EntryType.user);
        Entry entry4 = new Entry(4, new HashValue(SHA256, "itemhash3"), timestamp, "key3", EntryType.user);

        postgresDataAccessLayer.appendEntry(entry1);
        postgresDataAccessLayer.appendEntry(entry2);
        postgresDataAccessLayer.appendEntry(entry3);
        postgresDataAccessLayer.appendEntry(entry4);

        assertThat(postgresDataAccessLayer.getAllEntries().size(), is(4));
        assertThat(postgresDataAccessLayer.getAllEntries(), contains(entry4, entry3, entry2, entry1));
        assertThat(postgresDataAccessLayer.getEntries(1, 10).size(), is(4));
        assertThat(postgresDataAccessLayer.getEntries(1, 10), contains(entry1, entry2, entry3, entry4));
        assertThat(postgresDataAccessLayer.getEntries(2, 2).size(), is(2));
        assertThat(postgresDataAccessLayer.getEntries(2, 2), contains(entry2, entry3));
    }

    @Test
    public void canGetEntryIterator() {
        Instant timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Entry userEntry1 = new Entry(1, new HashValue(SHA256, "itemhash1"), timestamp, "key1", EntryType.user);
        Entry userEntry2 = new Entry(2, new HashValue(SHA256, "itemhash2"), timestamp, "key2", EntryType.user);
        Entry userEntry3 = new Entry(3, new HashValue(SHA256, "itemhash3"), timestamp, "key3", EntryType.user);
        Entry userEntry4 = new Entry(4, new HashValue(SHA256, "itemhash4"), timestamp, "key4", EntryType.user);
        Entry systemEntry1 = new Entry(1, new HashValue(SHA256, "itemhash1"), timestamp, "key1", EntryType.system);
        Entry systemEntry2 = new Entry(2, new HashValue(SHA256, "itemhash2"), timestamp, "key2", EntryType.system);
        Entry systemEntry3 = new Entry(3, new HashValue(SHA256, "itemhash3"), timestamp, "key3", EntryType.system);
        Entry systemEntry4 = new Entry(4, new HashValue(SHA256, "itemhash4"), timestamp, "key4", EntryType.system);

        postgresDataAccessLayer.appendEntry(userEntry1);
        postgresDataAccessLayer.appendEntry(userEntry2);
        postgresDataAccessLayer.appendEntry(userEntry3);
        postgresDataAccessLayer.appendEntry(userEntry4);
        postgresDataAccessLayer.appendEntry(systemEntry1);
        postgresDataAccessLayer.appendEntry(systemEntry2);
        postgresDataAccessLayer.appendEntry(systemEntry3);
        postgresDataAccessLayer.appendEntry(systemEntry4);

        assertThat(Lists.newArrayList(postgresDataAccessLayer.getEntryIterator(EntryType.user)).size(), is(4));
        assertThat(Lists.newArrayList(postgresDataAccessLayer.getEntryIterator(EntryType.user)), contains(userEntry1, userEntry2, userEntry3, userEntry4));
        assertThat(Lists.newArrayList(postgresDataAccessLayer.getEntryIterator(EntryType.user, 0, 10)).size(), is(4));
        assertThat(Lists.newArrayList(postgresDataAccessLayer.getEntryIterator(EntryType.user, 0, 10)), contains(userEntry1, userEntry2, userEntry3, userEntry4));
        assertThat(Lists.newArrayList(postgresDataAccessLayer.getEntryIterator(EntryType.user, 1, 3)).size(), is(2));
        assertThat(Lists.newArrayList(postgresDataAccessLayer.getEntryIterator(EntryType.user, 1, 3)), contains(userEntry2, userEntry3));

        assertThat(Lists.newArrayList(postgresDataAccessLayer.getEntryIterator(EntryType.system)).size(), is(4));
        assertThat(Lists.newArrayList(postgresDataAccessLayer.getEntryIterator(EntryType.system)), contains(systemEntry1, systemEntry2, systemEntry3, systemEntry4));
        assertThat(Lists.newArrayList(postgresDataAccessLayer.getEntryIterator(EntryType.system, 0, 10)).size(), is(4));
        assertThat(Lists.newArrayList(postgresDataAccessLayer.getEntryIterator(EntryType.system, 0, 10)), contains(systemEntry1, systemEntry2, systemEntry3, systemEntry4));
        assertThat(Lists.newArrayList(postgresDataAccessLayer.getEntryIterator(EntryType.system, 1, 3)).size(), is(2));
        assertThat(Lists.newArrayList(postgresDataAccessLayer.getEntryIterator(EntryType.system, 1, 3)), contains(systemEntry2, systemEntry3));
    }

    @Test
    public void canGetEntriesByKey() {
        Instant timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Entry entry1 = new Entry(1, new HashValue(SHA256, "itemhash1"), timestamp, "key1", EntryType.user);
        Entry entry2 = new Entry(2, new HashValue(SHA256, "itemhash2"), timestamp, "key2", EntryType.user);
        Entry entry3 = new Entry(3, new HashValue(SHA256, "itemhash3"), timestamp, "key3", EntryType.user);
        Entry entry4 = new Entry(4, new HashValue(SHA256, "itemhash4"), timestamp, "key3", EntryType.user);

        postgresDataAccessLayer.appendEntry(entry1);
        postgresDataAccessLayer.appendEntry(entry2);
        postgresDataAccessLayer.appendEntry(entry3);
        postgresDataAccessLayer.appendEntry(entry4);

        assertThat(postgresDataAccessLayer.getAllEntriesByKey("key1").size(), is(1));
        assertThat(postgresDataAccessLayer.getAllEntriesByKey("key1"), contains(entry1));
        assertThat(postgresDataAccessLayer.getAllEntriesByKey("key2").size(), is(1));
        assertThat(postgresDataAccessLayer.getAllEntriesByKey("key2"), contains(entry2));
        assertThat(postgresDataAccessLayer.getAllEntriesByKey("key3").size(), is(2));
        assertThat(postgresDataAccessLayer.getAllEntriesByKey("key3"), contains(entry3, entry4));
    }

    @Test
    public void canGetTotalEntries() {
        assertThat(postgresDataAccessLayer.getTotalEntries(EntryType.user), is(0));
        assertThat(postgresDataAccessLayer.getTotalEntries(EntryType.system), is(0));

        Instant timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Entry userEntry1 = new Entry(1, new HashValue(SHA256, "itemhash1"), timestamp, "key1", EntryType.user);
        Entry userEntry2 = new Entry(2, new HashValue(SHA256, "itemhash2"), timestamp, "key2", EntryType.user);
        Entry userEntry3 = new Entry(3, new HashValue(SHA256, "itemhash3"), timestamp, "key3", EntryType.user);
        Entry userEntry4 = new Entry(4, new HashValue(SHA256, "itemhash4"), timestamp, "key4", EntryType.user);

        postgresDataAccessLayer.appendEntry(userEntry1);
        postgresDataAccessLayer.appendEntry(userEntry2);
        postgresDataAccessLayer.appendEntry(userEntry3);
        postgresDataAccessLayer.appendEntry(userEntry4);

        assertThat(postgresDataAccessLayer.getTotalEntries(EntryType.user), is(4));

        Entry systemEntry1 = new Entry(1, new HashValue(SHA256, "itemhash1"), timestamp, "key1", EntryType.system);
        Entry systemEntry2 = new Entry(2, new HashValue(SHA256, "itemhash2"), timestamp, "key2", EntryType.system);

        postgresDataAccessLayer.appendEntry(systemEntry1);
        postgresDataAccessLayer.appendEntry(systemEntry2);

        assertThat(postgresDataAccessLayer.getTotalEntries(EntryType.system), is(2));
    }

    @Test
    public void canGetLastUpdatedTime() {
        assertThat(postgresDataAccessLayer.getLastUpdatedTime(), is(Optional.empty()));

        Instant timestamp1 = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant timestamp2 = timestamp1.plus(Duration.ofDays(1));
        Instant timestamp3 = timestamp2.plus(Duration.ofDays(1));

        Entry userEntry1 = new Entry(1, new HashValue(SHA256, "itemhash1"), timestamp1, "key1", EntryType.user);
        Entry userEntry2 = new Entry(2, new HashValue(SHA256, "itemhash2"), timestamp2, "key2", EntryType.user);
        Entry systemEntry1 = new Entry(1, new HashValue(SHA256, "itemhash1"), timestamp3, "key1", EntryType.system);

        postgresDataAccessLayer.appendEntry(userEntry1);
        postgresDataAccessLayer.appendEntry(userEntry2);
        postgresDataAccessLayer.appendEntry(systemEntry1);

        assertThat(postgresDataAccessLayer.getLastUpdatedTime(), is(Optional.of(timestamp2)));
    }

    @Test
    public void canAddAndGetItem() throws IOException {
        Item item1 = new Item(new HashValue(SHA256, "itemhash1"), objectMapper.readTree("{\"field\":\"foo\"}"));
        Item item2 = new Item(new HashValue(SHA256, "itemhash2"), objectMapper.readTree("{\"field\":\"bar\"}"));

        postgresDataAccessLayer.addItem(item1);
        postgresDataAccessLayer.addItem(item2);

        assertThat(postgresDataAccessLayer.getItem(new HashValue(SHA256, "itemhash1")), is(Optional.of(item1)));
        assertThat(postgresDataAccessLayer.getItem(new HashValue(SHA256, "itemhash2")), is(Optional.of(item2)));
        assertThat(postgresDataAccessLayer.getItem(new HashValue(SHA256, "itemhash3")), is(Optional.empty()));
    }

    @Test
    public void canAddItemsInBatch() throws IOException {
        Item item1 = new Item(new HashValue(SHA256, "itemhash1"), objectMapper.readTree("{\"field\":\"foo\"}"));
        Item item2 = new Item(new HashValue(SHA256, "itemhash2"), objectMapper.readTree("{\"field\":\"bar\"}"));

        postgresDataAccessLayer.addItems(Arrays.asList(item1, item2));

        assertThat(postgresDataAccessLayer.getItem(new HashValue(SHA256, "itemhash1")), is(Optional.of(item1)));
        assertThat(postgresDataAccessLayer.getItem(new HashValue(SHA256, "itemhash2")), is(Optional.of(item2)));
        assertThat(postgresDataAccessLayer.getItem(new HashValue(SHA256, "itemhash3")), is(Optional.empty()));
    }

    @Test
    public void doesNotDuplicateItems() throws IOException {
        Item item1 = new Item(new HashValue(SHA256, "itemhash1"), objectMapper.readTree("{\"field\":\"foo\"}"));
        Item item2 = new Item(new HashValue(SHA256, "itemhash2"), objectMapper.readTree("{\"field\":\"bar\"}"));

        postgresDataAccessLayer.addItem(item1);
        postgresDataAccessLayer.addItem(item2);
        postgresDataAccessLayer.addItem(item1);

        assertThat(postgresDataAccessLayer.getItem(new HashValue(SHA256, "itemhash1")), is(Optional.of(item1)));
        assertThat(postgresDataAccessLayer.getItem(new HashValue(SHA256, "itemhash2")), is(Optional.of(item2)));
        assertThat(postgresDataAccessLayer.getItem(new HashValue(SHA256, "itemhash3")), is(Optional.empty()));
    }

    @Test
    public void canGetMultipleItems() throws IOException {
        assertThat(postgresDataAccessLayer.getAllItems().size(), is(0));

        Item item1 = new Item(new HashValue(SHA256, "itemhash1"), objectMapper.readTree("{\"field\":\"foo\"}"));
        Item item2 = new Item(new HashValue(SHA256, "itemhash2"), objectMapper.readTree("{\"field\":\"bar\"}"));

        postgresDataAccessLayer.addItem(item1);
        postgresDataAccessLayer.addItem(item2);

        assertThat(postgresDataAccessLayer.getAllItems().size(), is(2));
        assertThat(postgresDataAccessLayer.getAllItems(), contains(item1, item2));
    }

    @Test
    public void canGetItemIterator() throws IOException {
        Instant timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Entry userEntry1 = new Entry(1, new HashValue(SHA256, "itemhash1"), timestamp, "key1", EntryType.user);
        Entry userEntry2 = new Entry(2, new HashValue(SHA256, "itemhash2"), timestamp, "key2", EntryType.user);
        Entry userEntry3 = new Entry(3, new HashValue(SHA256, "itemhash3"), timestamp, "key3", EntryType.user);
        Entry userEntry4 = new Entry(4, new HashValue(SHA256, "itemhash4"), timestamp, "key4", EntryType.user);
        Item item1 = new Item(new HashValue(SHA256, "itemhash1"), objectMapper.readTree("{\"field\":\"value1\"}"));
        Item item2 = new Item(new HashValue(SHA256, "itemhash2"), objectMapper.readTree("{\"field\":\"value2\"}"));
        Item item3 = new Item(new HashValue(SHA256, "itemhash3"), objectMapper.readTree("{\"field\":\"value3\"}"));
        Item item4 = new Item(new HashValue(SHA256, "itemhash4"), objectMapper.readTree("{\"field\":\"value4\"}"));

        Entry systemEntry1 = new Entry(1, new HashValue(SHA256, "itemhash5"), timestamp, "key5", EntryType.system);
        Entry systemEntry2 = new Entry(2, new HashValue(SHA256, "itemhash6"), timestamp, "key6", EntryType.system);
        Entry systemEntry3 = new Entry(3, new HashValue(SHA256, "itemhash7"), timestamp, "key7", EntryType.system);
        Entry systemEntry4 = new Entry(4, new HashValue(SHA256, "itemhash8"), timestamp, "key8", EntryType.system);
        Item item5 = new Item(new HashValue(SHA256, "itemhash5"), objectMapper.readTree("{\"field\":\"value5\"}"));
        Item item6 = new Item(new HashValue(SHA256, "itemhash6"), objectMapper.readTree("{\"field\":\"value6\"}"));
        Item item7 = new Item(new HashValue(SHA256, "itemhash7"), objectMapper.readTree("{\"field\":\"value7\"}"));
        Item item8 = new Item(new HashValue(SHA256, "itemhash8"), objectMapper.readTree("{\"field\":\"value8\"}"));

        postgresDataAccessLayer.appendEntry(userEntry1);
        postgresDataAccessLayer.appendEntry(userEntry2);
        postgresDataAccessLayer.appendEntry(userEntry3);
        postgresDataAccessLayer.appendEntry(userEntry4);
        postgresDataAccessLayer.addItem(item1);
        postgresDataAccessLayer.addItem(item2);
        postgresDataAccessLayer.addItem(item3);
        postgresDataAccessLayer.addItem(item4);

        postgresDataAccessLayer.appendEntry(systemEntry1);
        postgresDataAccessLayer.appendEntry(systemEntry2);
        postgresDataAccessLayer.appendEntry(systemEntry3);
        postgresDataAccessLayer.appendEntry(systemEntry4);
        postgresDataAccessLayer.addItem(item5);
        postgresDataAccessLayer.addItem(item6);
        postgresDataAccessLayer.addItem(item7);
        postgresDataAccessLayer.addItem(item8);

        assertThat(Lists.newArrayList(postgresDataAccessLayer.getItemIterator(EntryType.user)).size(), is(4));
        assertThat(Lists.newArrayList(postgresDataAccessLayer.getItemIterator(EntryType.user)), containsInAnyOrder(item1, item2, item3, item4));
        assertThat(Lists.newArrayList(postgresDataAccessLayer.getItemIterator(0, 10)).size(), is(4));
        assertThat(Lists.newArrayList(postgresDataAccessLayer.getItemIterator(0, 10)), containsInAnyOrder(item1, item2, item3, item4));
        assertThat(Lists.newArrayList(postgresDataAccessLayer.getItemIterator(1, 3)).size(), is(2));
        assertThat(Lists.newArrayList(postgresDataAccessLayer.getItemIterator(1, 3)), containsInAnyOrder(item2, item3));

        assertThat(Lists.newArrayList(postgresDataAccessLayer.getItemIterator(EntryType.system)).size(), is(4));
        assertThat(Lists.newArrayList(postgresDataAccessLayer.getItemIterator(EntryType.system)), containsInAnyOrder(item5, item6, item7, item8));
    }

    @Test
    public void canGetRecordsAndTotalRecords() throws IOException {
        Instant timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Entry userEntry1 = new Entry(1, new HashValue(SHA256, "itemhash1"), timestamp, "key1", EntryType.user);
        Entry userEntry2 = new Entry(2, new HashValue(SHA256, "itemhash2"), timestamp, "key2", EntryType.user);
        Entry userEntry3 = new Entry(3, new HashValue(SHA256, "itemhash3"), timestamp, "key2", EntryType.user);
        Item item1 = new Item(new HashValue(SHA256, "itemhash1"), objectMapper.readTree("{\"field\":\"value1\"}"));
        Item item2 = new Item(new HashValue(SHA256, "itemhash2"), objectMapper.readTree("{\"field\":\"value2\"}"));
        Item item3 = new Item(new HashValue(SHA256, "itemhash3"), objectMapper.readTree("{\"field\":\"value3\"}"));

        Entry systemEntry1 = new Entry(1, new HashValue(SHA256, "itemhash4"), timestamp, "key5", EntryType.system);
        Entry systemEntry2 = new Entry(2, new HashValue(SHA256, "itemhash5"), timestamp, "key6", EntryType.system);
        Entry systemEntry3 = new Entry(3, new HashValue(SHA256, "itemhash6"), timestamp, "key5", EntryType.system);
        Item item4 = new Item(new HashValue(SHA256, "itemhash4"), objectMapper.readTree("{\"field\":\"value4\"}"));
        Item item5 = new Item(new HashValue(SHA256, "itemhash5"), objectMapper.readTree("{\"field\":\"value5\"}"));
        Item item6 = new Item(new HashValue(SHA256, "itemhash6"), objectMapper.readTree("{\"field\":\"value6\"}"));

        postgresDataAccessLayer.appendEntry(userEntry1);
        postgresDataAccessLayer.appendEntry(userEntry2);
        postgresDataAccessLayer.appendEntry(userEntry3);
        postgresDataAccessLayer.addItem(item1);
        postgresDataAccessLayer.addItem(item2);
        postgresDataAccessLayer.addItem(item3);

        postgresDataAccessLayer.appendEntry(systemEntry1);
        postgresDataAccessLayer.appendEntry(systemEntry2);
        postgresDataAccessLayer.appendEntry(systemEntry3);
        postgresDataAccessLayer.addItem(item4);
        postgresDataAccessLayer.addItem(item5);
        postgresDataAccessLayer.addItem(item6);

        Record userRecord1 = new Record(userEntry1, item1);
        Record userRecord2 = new Record(userEntry3, item3);
        Record systemRecord1 = new Record(systemEntry2, item5);
        Record systemRecord2 = new Record(systemEntry3, item6);

        assertThat(postgresDataAccessLayer.getRecord(EntryType.user, "key1"), is(Optional.of(userRecord1)));
        assertThat(postgresDataAccessLayer.getRecord(EntryType.user, "key2"), is(Optional.of(userRecord2)));
        assertThat(postgresDataAccessLayer.getRecord(EntryType.user, "key3"), is(Optional.empty()));
        assertThat(postgresDataAccessLayer.getRecords(EntryType.user, 10, 0).size(), is(2));
        assertThat(postgresDataAccessLayer.getRecords(EntryType.user, 10, 0), contains(userRecord2, userRecord1));
        assertThat(postgresDataAccessLayer.getRecords(EntryType.user, 10, 1).size(), is(1));
        assertThat(postgresDataAccessLayer.getRecords(EntryType.user, 10, 1), contains(userRecord1));
        assertThat(postgresDataAccessLayer.getRecords(EntryType.user, 1, 0).size(), is(1));
        assertThat(postgresDataAccessLayer.getRecords(EntryType.user, 1, 0), contains(userRecord2));
        assertThat(postgresDataAccessLayer.getTotalRecords(EntryType.user), is(2));

        assertThat(postgresDataAccessLayer.getRecord(EntryType.system, "key1"), is(Optional.empty()));
        assertThat(postgresDataAccessLayer.getRecord(EntryType.system, "key5"), is(Optional.of(systemRecord2)));
        assertThat(postgresDataAccessLayer.getRecord(EntryType.system, "key6"), is(Optional.of(systemRecord1)));
        assertThat(postgresDataAccessLayer.getRecords(EntryType.system, 10, 0).size(), is(2));
        assertThat(postgresDataAccessLayer.getRecords(EntryType.system, 10, 0), contains(systemRecord2, systemRecord1));
        assertThat(postgresDataAccessLayer.getRecords(EntryType.system, 10, 1).size(), is(1));
        assertThat(postgresDataAccessLayer.getRecords(EntryType.system, 10, 1), contains(systemRecord1));
        assertThat(postgresDataAccessLayer.getRecords(EntryType.system, 1, 0).size(), is(1));
        assertThat(postgresDataAccessLayer.getRecords(EntryType.system, 1, 0), contains(systemRecord2));
        assertThat(postgresDataAccessLayer.getTotalRecords(EntryType.system), is(2));
    }

    @Test
    public void canGetRecordsByKeyValue() throws IOException {
        Instant timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Entry userEntry1 = new Entry(1, new HashValue(SHA256, "itemhash1"), timestamp, "key1", EntryType.user);
        Entry userEntry2 = new Entry(2, new HashValue(SHA256, "itemhash2"), timestamp, "key2", EntryType.user);
        Entry userEntry3 = new Entry(3, new HashValue(SHA256, "itemhash3"), timestamp, "key2", EntryType.user);
        Entry userEntry4 = new Entry(4, new HashValue(SHA256, "itemhash4"), timestamp, "key3", EntryType.user);
        Item item1 = new Item(new HashValue(SHA256, "itemhash1"), objectMapper.readTree("{\"field\":\"value1\"}"));
        Item item2 = new Item(new HashValue(SHA256, "itemhash2"), objectMapper.readTree("{\"field\":\"value1\"}"));
        Item item3 = new Item(new HashValue(SHA256, "itemhash3"), objectMapper.readTree("{\"field\":\"value2\"}"));
        Item item4 = new Item(new HashValue(SHA256, "itemhash4"), objectMapper.readTree("{\"field\":\"value2\"}"));

        Entry systemEntry1 = new Entry(1, new HashValue(SHA256, "itemhash5"), timestamp, "key5", EntryType.system);
        Entry systemEntry2 = new Entry(2, new HashValue(SHA256, "itemhash6"), timestamp, "key6", EntryType.system);
        Entry systemEntry3 = new Entry(3, new HashValue(SHA256, "itemhash7"), timestamp, "key5", EntryType.system);
        Item item5 = new Item(new HashValue(SHA256, "itemhash5"), objectMapper.readTree("{\"field\":\"value3\"}"));
        Item item6 = new Item(new HashValue(SHA256, "itemhash6"), objectMapper.readTree("{\"field\":\"value4\"}"));
        Item item7 = new Item(new HashValue(SHA256, "itemhash7"), objectMapper.readTree("{\"field\":\"value4\"}"));

        postgresDataAccessLayer.appendEntry(userEntry1);
        postgresDataAccessLayer.appendEntry(userEntry2);
        postgresDataAccessLayer.appendEntry(userEntry3);
        postgresDataAccessLayer.appendEntry(userEntry4);
        postgresDataAccessLayer.addItem(item1);
        postgresDataAccessLayer.addItem(item2);
        postgresDataAccessLayer.addItem(item3);
        postgresDataAccessLayer.addItem(item4);

        postgresDataAccessLayer.appendEntry(systemEntry1);
        postgresDataAccessLayer.appendEntry(systemEntry2);
        postgresDataAccessLayer.appendEntry(systemEntry3);
        postgresDataAccessLayer.addItem(item5);
        postgresDataAccessLayer.addItem(item6);
        postgresDataAccessLayer.addItem(item7);

        Record userRecord1 = new Record(userEntry1, item1);
        Record userRecord2 = new Record(userEntry3, item3);
        Record userRecord3 = new Record(userEntry4, item4);
        Record systemRecord1 = new Record(systemEntry2, item6);
        Record systemRecord2 = new Record(systemEntry3, item7);

        assertThat(postgresDataAccessLayer.findMax100RecordsByKeyValue(EntryType.user, "field", "value1").size(), is(1));
        assertThat(postgresDataAccessLayer.findMax100RecordsByKeyValue(EntryType.user, "field", "value1"), contains(userRecord1));
        assertThat(postgresDataAccessLayer.findMax100RecordsByKeyValue(EntryType.user, "field", "value2").size(), is(2));
        assertThat(postgresDataAccessLayer.findMax100RecordsByKeyValue(EntryType.user, "field", "value2"), contains(userRecord3, userRecord2));
        assertThat(postgresDataAccessLayer.findMax100RecordsByKeyValue(EntryType.user, "field", "value3").size(), is(0));
        assertThat(postgresDataAccessLayer.findMax100RecordsByKeyValue(EntryType.user, "invalid-field", "value").size(), is(0));

        assertThat(postgresDataAccessLayer.findMax100RecordsByKeyValue(EntryType.system, "field", "value3").size(), is(0));
        assertThat(postgresDataAccessLayer.findMax100RecordsByKeyValue(EntryType.system, "field", "value4").size(), is(2));
        assertThat(postgresDataAccessLayer.findMax100RecordsByKeyValue(EntryType.system, "field", "value4"), contains(systemRecord2, systemRecord1));
        assertThat(postgresDataAccessLayer.findMax100RecordsByKeyValue(EntryType.system, "invalid-field", "value").size(), is(0));
    }
}
