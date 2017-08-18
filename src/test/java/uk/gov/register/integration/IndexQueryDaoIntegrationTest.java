package uk.gov.register.integration;

import io.dropwizard.jdbi.OptionalContainerFactory;
import org.apache.log4j.MDC;
import org.assertj.core.util.Lists;
import org.junit.*;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.db.IndexQueryDAO;
import uk.gov.register.functional.app.WipeDatabaseRule;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static uk.gov.register.core.HashingAlgorithm.SHA256;
import static uk.gov.register.functional.app.TestRegister.address;
import static uk.gov.register.util.HashValue.decode;

/**
 * reg
 * {lae:BAS, lat:UA, name: Bath} hash = 6c4...
 * entry1 - 91,91,xxx6c4,BAS
 * <p>
 * [start all hashes with xxx so we can delete them all]
 * <p>
 * dervi
 * entry1 - 91,91,xxx6c4,UA
 * <p>
 * reg
 * {lae:BDD, lat:UA, name: Blackburn} 37d
 * entry2 - 92,92,xxx37d,BDD
 * <p>
 * dervi
 * entry2 - 92,92,[xxx6c4,xxx37d],UA
 * <p>
 * reg
 * {lae:BDF, lat:UA, name: Bedford} 01c
 * entry3 - 93,93,xxx01c,BDF
 * <p>
 * dervi
 * entry3 - 93,93,[xxx6c4,xxx37d,xxx01c],UA
 * <p>
 * reg
 * {lae:BIR, lat:MD, name: Birmingham} bdc
 * entry4 - 94,94,xxxbdc,BIR
 * <p>
 * deriv
 * entry4 - 94,94,[xxxbdc],MD
 * <p>
 * reg
 * {lae:BAS, lat:UA, name: New Bath} 12
 * entry5 - 95,95,xxx126,BAS
 * <p>
 * deriv
 * entry5 - 95,95,[xxx37d,xxx01c,xxx126],UA
 * <p>
 * reg
 * {lae:BDD, lat:MD, name: Blackburn} 509
 * entry6 - 96,96,xxx509,BDD
 * <p>
 * deriv
 * entry6 - 96,96,[xxx01c,xxx126],UA
 * entry7 - 97,96,[xxxbdc,xxx509],MD
 */
public class IndexQueryDaoIntegrationTest {
    private DBI dbi;
    private Handle handle;
    private String schema = address.getSchema();
    private IndexQueryDAO dao;
    private Instant timestamp = Instant.ofEpochMilli(1490610633L * 1000L);

    @Rule
    public WipeDatabaseRule wipeDatabaseRule = new WipeDatabaseRule(address);

    @Before
    public void setup() {
        MDC.put("register", schema);
        dbi = new DBI(address.getDatabaseConnectionString("PGRegisterTxnFT"));
        dbi.registerContainerFactory(new OptionalContainerFactory());
        handle = dbi.open();
        dao = handle.attach(IndexQueryDAO.class);
    }

    @After
    public void teardown() {
        dbi.close(handle);
    }

    @Test
    public void shouldReadRecordForUA() {
        nameChangeAndGroupChangeScenario();

        List<Record> recordList = dao.findRecords(Arrays.asList("UA"), "by-type", schema, "entry");

        assertThat(recordList.size(), equalTo(1));
        Record record = recordList.get(0);
        assertThat(record.getItems().size(), is(2));
        assertThat(record.getEntry().getEntryNumber(), is(96));
        assertThat(record.getEntry().getIndexEntryNumber(), is(96));

        HashValue hash01c = decode(SHA256, "sha-256:xxx01c");
        assertTrue(record.getItems().stream().anyMatch(i -> i.getSha256hex().equals(hash01c)));
        Item item = record.getItems().stream().filter(i -> i.getSha256hex().equals(hash01c)).findFirst().get();
        assertThat(item.getValue("name").get(), is("Bedford"));

        HashValue hash126 = decode(SHA256, "sha-256:xxx126");
        assertTrue(record.getItems().stream().anyMatch(i -> i.getSha256hex().equals(hash126)));
        Item item2 = record.getItems().stream().filter(i -> i.getSha256hex().equals(hash126)).findFirst().get();
        assertThat(item2.getValue("name").get(), is("New Bath"));
    }

    @Test
    public void shouldReadRecordForMD() {
        nameChangeAndGroupChangeScenario();

        List<Record> recordList = dao.findRecords(Arrays.asList("MD"), "by-type", schema, "entry");

        assertThat(recordList.size(), equalTo(1));
        Record record = recordList.get(0);
        assertThat(record.getItems().size(), is(2));
        assertThat(record.getEntry().getEntryNumber(), is(96));
        assertThat(record.getEntry().getIndexEntryNumber(), is(97));

        HashValue hash1 = decode(SHA256, "sha-256:xxxbdc");
        assertTrue("record should contain key sha-256:xxxbdc", record.getItems().stream().anyMatch(i -> i.getSha256hex().equals(hash1)));
        Item item = record.getItems().stream().filter(i -> i.getSha256hex().equals(hash1)).findFirst().get();
        assertThat(item.getValue("name").get(), is("Birmingham"));

        HashValue hash2 = decode(SHA256, "sha-256:xxx509");
        assertTrue("record should contain key sha-256:xxx509", record.getItems().stream().anyMatch(i -> i.getSha256hex().equals(hash2)));
        Item item2 = record.getItems().stream().filter(i -> i.getSha256hex().equals(hash2)).findFirst().get();
        assertThat(item2.getValue("name").get(), is("Blackburn with Darwen"));
    }

    @Test
    public void shouldReturnEmptyForMissingKey() {
        nameChangeAndGroupChangeScenario();
        List<Record> recordList = dao.findRecords(Arrays.asList("Z"), "by-type", schema, "entry");
        assertTrue("should be empty for key Z", recordList.isEmpty());
    }

    @Test
    public void shouldReturnEmptyForMissingIndex() {
        nameChangeAndGroupChangeScenario();
        List<Record> records = dao.findRecords(10, 0, "zzz", schema, "entry");
        assertThat(records.size(), is(0));
    }
    
    @Test
    public void shouldReadRecords() {
        nameChangeAndGroupChangeScenario();

        List<Record> records = dao.findRecords(10, 0, "by-type", schema, "entry");

        assertThat(records.size(), is(2));

        Record record0 = records.get(0);

        assertThat(record0.getItems().size(), is(2));
        assertThat(record0.getEntry().getEntryNumber(), is(96));
        assertThat(record0.getEntry().getIndexEntryNumber(), is(97));

        HashValue hash1 = decode(SHA256, "sha-256:xxxbdc");
        assertTrue(record0.getItems().stream().anyMatch(i -> i.getSha256hex().equals(hash1)));
        Item item1 = record0.getItems().stream().filter(i -> i.getSha256hex().equals(hash1)).findFirst().get();
        assertThat(item1.getValue("name").get(), is("Birmingham"));

        HashValue hash2 = decode(SHA256, "sha-256:xxx509");
        assertTrue(record0.getItems().stream().anyMatch(i -> i.getSha256hex().equals(hash2)));
        Item item2 = record0.getItems().stream().filter(i -> i.getSha256hex().equals(hash2)).findFirst().get();
        assertThat(item2.getValue("name").get(), is("Blackburn with Darwen"));

        Record record1 = records.get(1);
        assertThat(record1.getEntry().getEntryNumber(), is(96));
        assertThat(record1.getEntry().getIndexEntryNumber(), is(96));

        HashValue hash01c = decode(SHA256, "sha-256:xxx01c");
        assertTrue(record1.getItems().stream().anyMatch(i -> i.getSha256hex().equals(hash01c)));
        Item item3 = record1.getItems().stream().filter(i -> i.getSha256hex().equals(hash01c)).findFirst().get();
        assertThat(item3.getValue("name").get(), is("Bedford"));

        HashValue hash126 = decode(SHA256, "sha-256:xxx126");
        assertTrue(record1.getItems().stream().anyMatch(i -> i.getSha256hex().equals(hash126)));
        Item item4 = record1.getItems().stream().filter(i -> i.getSha256hex().equals(hash126)).findFirst().get();
        assertThat(item4.getValue("name").get(), is("New Bath"));

    }

    @Test
    public void shouldReadEntries() {
        nameChangeAndGroupChangeScenario();

        Iterator<Entry> entryIterator = dao.getIterator("by-type", schema, "entry");
        List<Entry> entries = Lists.newArrayList(entryIterator);

        assertThat(entries.size(), is(7));
        Entry entry0 = entries.get(0);
        Entry expectedEntry0 = new Entry(91, 91,
                Arrays.asList(decode(SHA256, "sha-256:xxx6c4")), timestamp, "UA", EntryType.user);
        assertThat(entry0, equalTo(expectedEntry0));

        Entry entryLast = entries.get(6);
        Entry expectedEntryLast = new Entry(97, 96,
                Arrays.asList(decode(SHA256, "sha-256:xxxbdc"),
                        decode(SHA256, "sha-256:xxx509")), timestamp, "MD", EntryType.user);
        assertThat(entryLast, equalTo(expectedEntryLast));
    }

    @Test
    public void shouldReadEntriesBetweenEntryNumbers() {
        nameChangeAndGroupChangeScenario();

        Iterator<Entry> entryIterator = dao.getIterator("by-type", 92, 94, schema, "entry");
        List<Entry> entries = Lists.newArrayList(entryIterator);

        assertThat(entries.size(), is(2));

        Entry entry0 = entries.get(0);
        Entry expectedEntry0 = new Entry(93, 93,
                Arrays.asList(decode(SHA256, "sha-256:xxx6c4"),
                        decode(SHA256, "sha-256:xxx37d"),
                        decode(SHA256, "sha-256:xxx01c")), timestamp, "UA", EntryType.user);
        assertThat(entry0, equalTo(expectedEntry0));

        Entry entryLast = entries.get(1);
        Entry expectedEntryLast = new Entry(94, 94,
                Arrays.asList(decode(SHA256, "sha-256:xxxbdc")), timestamp, "MD", EntryType.user);
        assertThat(entryLast, equalTo(expectedEntryLast));
    }

    @Test
    public void shouldReturnEmptyListForInvalidEntryNumberRange() {
        nameChangeAndGroupChangeScenario();

        Iterator<Entry> entryIterator = dao.getIterator("by-type", 94, 92, schema, "entry");
        List<Entry> entries = Lists.newArrayList(entryIterator);

        assertThat(entries.size(), is(0));
    }

    @Test
    public void shouldNotReturnResultsForUnknownName() {
        nameChangeAndGroupChangeScenario();

        Iterator<Entry> entryIterator = dao.getIterator("sam", schema, "entry");
        List<Entry> entries = Lists.newArrayList(entryIterator);

        assertThat(entries.size(), is(0));
    }

    @Test
    public void getTotalRecords_shouldReturnZero_whenNoRecordsExist() {
        int totalRecords = dao.getTotalRecords("by-type", schema);

        assertThat(totalRecords, is(0));
    }

    @Test
    public void getTotalRecords_shouldReturnTotalRecords_whenRecordsExist() {
        nameChangeAndGroupChangeScenario();
        int totalRecords = dao.getTotalRecords("by-type", schema);

        assertThat(totalRecords, is(2));
    }

    @Ignore("Ignore until we decide how to express records which existed before but no longer exist")
    public void shouldFindZeroItemsRecordForUA() {
        zeroItemsEntryScenario();

        List<Record> recordList = dao.findRecords(Arrays.asList("UA"), "by-type", schema, "entry");

        assertThat(recordList.size(), equalTo(1));
        Record record = recordList.get(0);

        assertThat(record.getItems().size(), is(0));
        assertThat(record.getEntry().getEntryNumber(), is(92));
        assertThat(record.getEntry().getIndexEntryNumber(), is(92));
    }

    @Test
    public void shouldFindZeroEntriesForUA() {
        zeroItemsEntryScenario();

        List<Entry> entries = Lists.newArrayList(dao.getIterator("by-type", schema, "entry"));

        assertThat(entries.size(), is(3));

        Entry entry1 = entries.get(1);
        Entry expectedEntry1 = new Entry(92, 92, Collections.emptyList(), timestamp, "UA", EntryType.user);
        assertThat(entry1, equalTo(expectedEntry1));

    }

    @Test
    public void shouldNotCountRecordsWithNoItems() {
        zeroItemsEntryScenario();
        int totalRecords = dao.getTotalRecords("by-type", schema);

        assertThat(totalRecords, is(1));
    }

    private void nameChangeAndGroupChangeScenario() {
        insertItems();
        insertEntries();
        insertIndex();
    }

    private void zeroItemsEntryScenario() {
        insertItems();
        insertEntriesZeroItems();
        insertIndexZeroItems();
    }


    private void insertItems() {
        handle.execute("INSERT INTO address.item (sha256hex,content)" +
                " VALUES ( 'xxx37d','{\"name\": \"Blackburn with Darwen\", \"local-authority-eng\": \"BBD\", \"local-authority-type\": \"UA\"}')");
        handle.execute("INSERT INTO address.item (sha256hex,content) " +
                "VALUES ( 'xxxbdc','{\"name\": \"Birmingham\", \"local-authority-eng\": \"BIR\", \"local-authority-type\": \"MD\"}')");
        handle.execute("INSERT INTO address.item (sha256hex,content) " +
                "VALUES ( 'xxx6c4','{\"name\": \"Bath and North East Somerset\", \"local-authority-eng\": \"BAS\", \"local-authority-type\": \"UA\"}')");
        handle.execute("INSERT INTO address.item (sha256hex,content) " +
                "VALUES ( 'xxx01c','{\"name\": \"Bedford\", \"local-authority-eng\": \"BDF\", \"local-authority-type\": \"UA\"}')");
        //new bath
        handle.execute("INSERT INTO address.item (sha256hex,content) " +
                "VALUES ( 'xxx126','{\"name\": \"New Bath\", \"local-authority-eng\": \"BAS\", \"local-authority-type\": \"UA\"}')");
        // BDD as MD
        handle.execute("INSERT INTO address.item (sha256hex,content) " +
                "VALUES ( 'xxx509','{\"name\": \"Blackburn with Darwen\", \"local-authority-eng\": \"BBD\", \"local-authority-type\": \"MD\"}')");

    }

    private void insertEntries() {
        // Ordinary entries for Bath, Blackburn, Bedford, Birmingham
        handle.execute("INSERT INTO address.entry (entry_number, \"timestamp\", \"key\", \"type\") VALUES (92, 1490610633, 'BBD', 'user'::address.ENTRY_TYPE)");
        handle.execute("INSERT INTO address.entry (entry_number, \"timestamp\", \"key\", \"type\") VALUES (93, 1490610633, 'BDF', 'user'::address.ENTRY_TYPE)");
        handle.execute("INSERT INTO address.entry (entry_number, \"timestamp\", \"key\", \"type\") VALUES (91, 1490610633, 'BAS', 'user'::address.ENTRY_TYPE)");
        handle.execute("INSERT INTO address.entry (entry_number, \"timestamp\", \"key\", \"type\") VALUES (94, 1490610633, 'BIR', 'user'::address.ENTRY_TYPE)");
        // Bath to New Bath
        handle.execute("INSERT INTO address.entry (entry_number, \"timestamp\", \"key\", \"type\") VALUES (95, 1490610633, 'BAS', 'user'::address.ENTRY_TYPE)");
        // Blackburn as MD
        handle.execute("INSERT INTO address.entry (entry_number, \"timestamp\", \"key\", \"type\") VALUES (96, 1490610633, 'BBD', 'user'::address.ENTRY_TYPE)");
        // join table
        handle.execute("INSERT INTO address.entry_item (entry_number, sha256hex) VALUES ( 91, 'xxx6c4')");
        handle.execute("INSERT INTO address.entry_item (entry_number, sha256hex) VALUES ( 92, 'xxx37d')");
        handle.execute("INSERT INTO address.entry_item (entry_number, sha256hex) VALUES ( 93, 'xxx01c')");
        handle.execute("INSERT INTO address.entry_item (entry_number, sha256hex) VALUES ( 94, 'xxxbdc')");
        handle.execute("INSERT INTO address.entry_item (entry_number, sha256hex) VALUES ( 95, 'xxx126')");
        handle.execute("INSERT INTO address.entry_item (entry_number, sha256hex) VALUES ( 96, 'xxx509')");

    }

    private void insertEntriesZeroItems() {
        // Blackburn as UA
        handle.execute("INSERT INTO address.entry (entry_number, \"timestamp\", \"key\", \"type\") VALUES (91, 1490610633, 'BBD', 'user'::address.ENTRY_TYPE)");
        // Blackburn as MD
        handle.execute("INSERT INTO address.entry (entry_number, \"timestamp\", \"key\", \"type\") VALUES (92, 1490610633, 'BBD', 'user'::address.ENTRY_TYPE)");
        // join table
        handle.execute("INSERT INTO address.entry_item (entry_number, sha256hex) VALUES ( 91, 'xxx37d')");
        handle.execute("INSERT INTO address.entry_item (entry_number, sha256hex) VALUES ( 92, 'xxx509')");

    }

    private void insertIndex() {
        // BAS
        handle.execute("INSERT INTO address.\"index\" (\"name\", \"key\", sha256hex, start_entry_number, end_entry_number, start_index_entry_number, end_index_entry_number)" +
                " VALUES('by-type', 'UA', 'xxx6c4', 91, NULL, 91, NULL)");
        // BBD
        handle.execute("INSERT INTO address.\"index\" (\"name\", \"key\", sha256hex, start_entry_number, end_entry_number, start_index_entry_number, end_index_entry_number) " +
                "VALUES('by-type', 'UA', 'xxx37d', 92, NULL, 92, NULL)");
        // BDF
        handle.execute("INSERT INTO address.\"index\" (\"name\", \"key\", sha256hex, start_entry_number, end_entry_number, start_index_entry_number, end_index_entry_number)" +
                " VALUES('by-type', 'UA', 'xxx01c', 93, NULL, 93, NULL)");
        // BIR
        handle.execute("INSERT INTO address.\"index\" (\"name\", \"key\", sha256hex, start_entry_number, end_entry_number, start_index_entry_number, end_index_entry_number)" +
                " VALUES('by-type', 'MD', 'xxxbdc', 94, NULL, 94, NULL)");
        // update bath to new bath
        handle.execute("INSERT INTO address.\"index\" (\"name\", \"key\", sha256hex, start_entry_number, end_entry_number, start_index_entry_number, end_index_entry_number) " +
                "VALUES('by-type', 'UA', 'xxx126', 95, NULL, 95, NULL)");

        handle.execute("UPDATE address.\"index\" set end_entry_number = 95, end_index_entry_number = 95 where name='by-type' and sha256hex='xxx6c4'");

        // change BBD from UA to MD
        handle.execute("UPDATE address.\"index\" set end_index_entry_number=96, end_entry_number=96 where name='by-type' and sha256hex='xxx37d'");

        handle.execute("INSERT INTO address.\"index\" (\"name\", \"key\", sha256hex, start_entry_number, end_entry_number, start_index_entry_number, end_index_entry_number)" +
                " VALUES('by-type', 'MD', 'xxx509', 96, NULL, 97, NULL)");

    }

    private void insertIndexZeroItems() {
        handle.execute("INSERT INTO address.\"index\" (\"name\", \"key\", sha256hex, start_entry_number, end_entry_number, start_index_entry_number, end_index_entry_number)" +
                " VALUES('by-type', 'UA', 'xxx37d', 91, NULL, 91, NULL)");
        // change BBD from UA to MD
        handle.execute("UPDATE address.\"index\" set end_index_entry_number=92, end_entry_number=92 where name='by-type' and sha256hex='xxx37d'");

        handle.execute("INSERT INTO address.\"index\" (\"name\", \"key\", sha256hex, start_entry_number, end_entry_number, start_index_entry_number, end_index_entry_number)" +
                " VALUES('by-type', 'MD', 'xxx509', 92, NULL, 93, NULL)");
    }

}
