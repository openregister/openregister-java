package uk.gov.register.integration;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.register.core.HashingAlgorithm.SHA256;
import static uk.gov.register.util.HashValue.decode;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.db.IndexQueryDAO;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import io.dropwizard.jdbi.OptionalContainerFactory;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

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
 * {lae:BDD, lat:UA, name: New Bath} 12
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
    private IndexQueryDAO dao;
    private Instant timestamp = Instant.ofEpochMilli(1490610633L * 1000L);

    @Before
    public void setup() {
        dbi = new DBI("jdbc:postgresql://localhost:5432/ft_openregister_java_address?user=postgres&ApplicationName=PGRegisterTxnFT");
        dbi.registerContainerFactory(new OptionalContainerFactory());
        handle = dbi.open();
        dao = handle.attach(IndexQueryDAO.class);
    }

    @After
    public void teardown() {
        handle.execute("delete from public.item where sha256hex like 'xxx%' ");
        handle.execute("delete from public.entry where sha256hex like 'xxx%' ");
        handle.execute("delete from public.entry_item where sha256hex like 'xxx%' ");
        handle.execute("delete from public.index where name = 'by-type' ");
        dbi.close(handle);
    }

    @Test
    public void shouldReadRecordForUA() {
        nameChangeAndGroupChangeScenario();

        Optional<Record> recordOptional = dao.findRecord("UA", "by-type");

        assertTrue(recordOptional.isPresent());
        Record record = recordOptional.get();
        assertThat(record.getItems().size(), is(2));
        assertThat(record.getEntry().getEntryNumber(), is(96));
        assertThat(record.getEntry().getIndexEntryNumber(), is(96));

        HashValue hash01c = decode(SHA256, "sha-256:xxx01c");
        assertTrue(record.getItems().containsKey(hash01c));
        Item item = record.getItems().get(hash01c);
        assertThat(item.getValue("name").get(), is("Bedford"));

        HashValue hash126 = decode(SHA256, "sha-256:xxx126");
        assertTrue(record.getItems().containsKey(hash126));
        Item item2 = record.getItems().get(hash126);
        assertThat(item2.getValue("name").get(), is("New Bath"));
    }

    @Test
    public void shouldReadRecordForMD() {
        nameChangeAndGroupChangeScenario();

        Optional<Record> recordOptional = dao.findRecord("MD", "by-type");

        assertTrue(recordOptional.isPresent());
        Record record = recordOptional.get();
        assertThat(record.getItems().size(), is(2));
        assertThat(record.getEntry().getEntryNumber(), is(96));
        assertThat(record.getEntry().getIndexEntryNumber(), is(97));

        HashValue hash1 = decode(SHA256, "sha-256:xxxbdc");
        assertTrue("record should contain key sha-256:xxxbdc", record.getItems().containsKey(hash1));
        Item item = record.getItems().get(hash1);
        assertThat(item.getValue("name").get(), is("Birmingham"));

        HashValue hash2 = decode(SHA256, "sha-256:xxx509");
        assertTrue("record should contain key sha-256:xxx509", record.getItems().containsKey(hash2));
        Item item2 = record.getItems().get(hash2);
        assertThat(item2.getValue("name").get(), is("Blackburn with Darwen"));
    }

    @Test
    public void shouldReturnEmptyForMissingKey() {
        nameChangeAndGroupChangeScenario();
        Optional<Record> recordOptional = dao.findRecord("Z", "by-type");
        assertFalse("should be empty for key Z", recordOptional.isPresent());
    }

    @Test
    public void shouldReturnEmptyForMissingIndex() {
        nameChangeAndGroupChangeScenario();
        List<Record> records = dao.findRecords(10, 0, "zzz");
        assertThat(records.size(), is(0));
    }


    @Test
    public void shouldReadRecords() {
        nameChangeAndGroupChangeScenario();

        List<Record> records = dao.findRecords(10, 0, "by-type");

        assertThat(records.size(), is(2));

        Record record0 = records.get(0);

        assertThat(record0.getItems().size(), is(2));
        assertThat(record0.getEntry().getEntryNumber(), is(96));
        assertThat(record0.getEntry().getIndexEntryNumber(), is(97));

        HashValue hash1 = decode(SHA256, "sha-256:xxxbdc");
        assertTrue(record0.getItems().containsKey(hash1));
        Item item1 = record0.getItems().get(hash1);
        assertThat(item1.getValue("name").get(), is("Birmingham"));

        HashValue hash2 = decode(SHA256, "sha-256:xxx509");
        assertTrue(record0.getItems().containsKey(hash2));
        Item item2 = record0.getItems().get(hash2);
        assertThat(item2.getValue("name").get(), is("Blackburn with Darwen"));

        Record record1 = records.get(1);
        assertThat(record1.getEntry().getEntryNumber(), is(96));
        assertThat(record1.getEntry().getIndexEntryNumber(), is(96));

        HashValue hash01c = decode(SHA256, "sha-256:xxx01c");
        assertTrue(record1.getItems().containsKey(hash01c));
        Item item3 = record1.getItems().get(hash01c);
        assertThat(item3.getValue("name").get(), is("Bedford"));

        HashValue hash126 = decode(SHA256, "sha-256:xxx126");
        assertTrue(record1.getItems().containsKey(hash126));
        Item item4 = record1.getItems().get(hash126);
        assertThat(item4.getValue("name").get(), is("New Bath"));

    }

    @Test
    public void shouldReadEntries() {
        nameChangeAndGroupChangeScenario();

        Iterator<Entry> entryIterator = dao.getIterator("by-type");
        List<Entry> entries = Lists.newArrayList(entryIterator);

        assertThat(entries.size(), is(7));
        Entry entry0 = entries.get(0);
        Entry expectedEntry0 = new Entry(91, 91,
                Arrays.asList(decode(SHA256, "sha-256:xxx6c4")), timestamp, "UA");
        assertThat(entry0, equalTo(expectedEntry0));

        Entry entryLast = entries.get(6);
        Entry expectedEntryLast = new Entry(97, 96,
                Arrays.asList(decode(SHA256, "sha-256:xxxbdc"),
                        decode(SHA256, "sha-256:xxx509")), timestamp, "MD");
        assertThat(entryLast, equalTo(expectedEntryLast));
    }

    @Test
    public void shouldReadEntriesBetweenEntryNumbers() {
        nameChangeAndGroupChangeScenario();

        Iterator<Entry> entryIterator = dao.getIterator("by-type", 92, 94);
        List<Entry> entries = Lists.newArrayList(entryIterator);

        assertThat(entries.size(), is(2));

        Entry entry0 = entries.get(0);
        Entry expectedEntry0 = new Entry(93, 93,
                Arrays.asList(decode(SHA256, "sha-256:xxx6c4"),
                        decode(SHA256, "sha-256:xxx37d"),
                        decode(SHA256, "sha-256:xxx01c")), timestamp, "UA");
        assertThat(entry0, equalTo(expectedEntry0));

        Entry entryLast = entries.get(1);
        Entry expectedEntryLast = new Entry(94, 94,
                Arrays.asList(decode(SHA256, "sha-256:xxxbdc")), timestamp, "MD");
        assertThat(entryLast, equalTo(expectedEntryLast));
    }

    @Test
    public void shouldReturnEmptyListForInvalidEntryNumberRange() {
        nameChangeAndGroupChangeScenario();

        Iterator<Entry> entryIterator = dao.getIterator("by-type", 94, 92);
        List<Entry> entries = Lists.newArrayList(entryIterator);

        assertThat(entries.size(), is(0));
    }

    @Test
    public void shouldNotReturnResultsForUnknownName() {
        nameChangeAndGroupChangeScenario();

        Iterator<Entry> entryIterator = dao.getIterator("sam");
        List<Entry> entries = Lists.newArrayList(entryIterator);

        assertThat(entries.size(), is(0));
    }

    @Test
    public void shouldFindZeroItemsRecordForUA() {
        zeroItemsEntryScenario();

        Optional<Record> recordOptional = dao.findRecord("UA", "by-type");

        assertTrue(recordOptional.isPresent());
        Record record = recordOptional.get();

        assertThat(record.getItems().size(), is(0));
        assertThat(record.getEntry().getEntryNumber(), is(92));
        assertThat(record.getEntry().getIndexEntryNumber(), is(92));

    }

    @Test
    public void shouldFindZeroEntriesForUA() {
        zeroItemsEntryScenario();

        List<Entry> entries = Lists.newArrayList(dao.getIterator("by-type"));

        assertThat(entries.size(), is(3));

        Entry entry1 = entries.get(1);
        Entry expectedEntry1 = new Entry(92, 92,
                Collections.emptyList(), timestamp, "UA");
        assertThat(entry1, equalTo(expectedEntry1));

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
        handle.execute("INSERT INTO public.item (sha256hex,content)" +
                " VALUES ( 'xxx37d','{\"name\": \"Blackburn with Darwen\", \"local-authority-eng\": \"BBD\", \"local-authority-type\": \"UA\"}')");
        handle.execute("INSERT INTO public.item (sha256hex,content) " +
                "VALUES ( 'xxxbdc','{\"name\": \"Birmingham\", \"local-authority-eng\": \"BIR\", \"local-authority-type\": \"MD\"}')");
        handle.execute("INSERT INTO public.item (sha256hex,content) " +
                "VALUES ( 'xxx6c4','{\"name\": \"Bath and North East Somerset\", \"local-authority-eng\": \"BAS\", \"local-authority-type\": \"UA\"}')");
        handle.execute("INSERT INTO public.item (sha256hex,content) " +
                "VALUES ( 'xxx01c','{\"name\": \"Bedford\", \"local-authority-eng\": \"BDF\", \"local-authority-type\": \"UA\"}')");
        //new bath
        handle.execute("INSERT INTO public.item (sha256hex,content) " +
                "VALUES ( 'xxx126','{\"name\": \"New Bath\", \"local-authority-eng\": \"BAS\", \"local-authority-type\": \"UA\"}')");
        // BDD as MD
        handle.execute("INSERT INTO public.item (sha256hex,content) " +
                "VALUES ( 'xxx509','{\"name\": \"Blackburn with Darwen\", \"local-authority-eng\": \"BBD\", \"local-authority-type\": \"MD\"}')");

    }

    private void insertEntries() {
        // Ordinary entries for Bath, Blackburn, Bedford, Birmingham
        handle.execute("INSERT INTO public.entry (entry_number,sha256hex,\"timestamp\",\"key\") VALUES ( 91,'xxx6c4',1490610633,'BAS')");
        handle.execute("INSERT INTO public.entry (entry_number,sha256hex,\"timestamp\",\"key\") VALUES ( 92,'xxx37d',1490610633,'BBD')");
        handle.execute("INSERT INTO public.entry (entry_number,sha256hex,\"timestamp\",\"key\") VALUES ( 93,'xxx01c',1490610633,'BDF')");
        handle.execute("INSERT INTO public.entry (entry_number,sha256hex,\"timestamp\",\"key\") VALUES ( 94,'xxxbdc',1490610633,'BIR')");
        // Bath to New Bath
        handle.execute("INSERT INTO public.entry (entry_number, sha256hex, \"timestamp\", \"key\") VALUES ( 95, 'xxx126', 1490610633, 'BAS')");
        // Blackburn as MD
        handle.execute("INSERT INTO public.entry (entry_number, sha256hex, \"timestamp\", \"key\") VALUES ( 96, 'xxx509', 1490610633, 'BBD')");
        // join table
        handle.execute("INSERT INTO public.entry_item (entry_number,sha256hex) VALUES ( 91,'xxx6c4')");
        handle.execute("INSERT INTO public.entry_item (entry_number,sha256hex) VALUES ( 92,'xxx37d')");
        handle.execute("INSERT INTO public.entry_item (entry_number,sha256hex) VALUES ( 93,'xxx01c')");
        handle.execute("INSERT INTO public.entry_item (entry_number,sha256hex) VALUES ( 94,'xxxbdc')");
        handle.execute("INSERT INTO public.entry_item (entry_number,sha256hex) VALUES ( 95,'xxx126')");
        handle.execute("INSERT INTO public.entry_item (entry_number,sha256hex) VALUES ( 96,'xxx509')");

    }

    private void insertEntriesZeroItems() {
        // Blackburn as UA
        handle.execute("INSERT INTO public.entry (entry_number,sha256hex,\"timestamp\",\"key\") VALUES ( 91,'xxx37d',1490610633,'BBD')");
        // Blackburn as MD
        handle.execute("INSERT INTO public.entry (entry_number, sha256hex, \"timestamp\", \"key\") VALUES ( 92, 'xxx509', 1490610633, 'BBD')");
        // join table
        handle.execute("INSERT INTO public.entry_item (entry_number,sha256hex) VALUES ( 91,'xxx37d')");
        handle.execute("INSERT INTO public.entry_item (entry_number,sha256hex) VALUES ( 92,'xxx509')");

    }

    private void insertIndex() {
        // BAS
        handle.execute("INSERT INTO public.\"index\" (\"name\", \"key\", sha256hex, start_entry_number, end_entry_number, start_index_entry_number, end_index_entry_number)" +
                " VALUES('by-type', 'UA', 'xxx6c4', 91, NULL, 91, NULL)");
        // BBD
        handle.execute("INSERT INTO public.\"index\" (\"name\", \"key\", sha256hex, start_entry_number, end_entry_number, start_index_entry_number, end_index_entry_number) " +
                "VALUES('by-type', 'UA', 'xxx37d', 92, NULL, 92, NULL)");
        // BDF
        handle.execute("INSERT INTO public.\"index\" (\"name\", \"key\", sha256hex, start_entry_number, end_entry_number, start_index_entry_number, end_index_entry_number)" +
                " VALUES('by-type', 'UA', 'xxx01c', 93, NULL, 93, NULL)");
        // BIR
        handle.execute("INSERT INTO public.\"index\" (\"name\", \"key\", sha256hex, start_entry_number, end_entry_number, start_index_entry_number, end_index_entry_number)" +
                " VALUES('by-type', 'MD', 'xxxbdc', 94, NULL, 94, NULL)");
        // update bath to new bath
        handle.execute("INSERT INTO public.\"index\" (\"name\", \"key\", sha256hex, start_entry_number, end_entry_number, start_index_entry_number, end_index_entry_number) " +
                "VALUES('by-type', 'UA', 'xxx126', 95, NULL, 95, NULL)");

        handle.execute("UPDATE public.\"index\" set end_entry_number = 95, end_index_entry_number = 95 where name='by-type' and sha256hex='xxx6c4'");

        // change BBD from UA to MD
        handle.execute("UPDATE public.\"index\" set end_index_entry_number=96, end_entry_number=96 where name='by-type' and sha256hex='xxx37d'");

        handle.execute("INSERT INTO public.\"index\" (\"name\", \"key\", sha256hex, start_entry_number, end_entry_number, start_index_entry_number, end_index_entry_number)" +
                " VALUES('by-type', 'MD', 'xxx509', 96, NULL, 97, NULL)");

    }

    private void insertIndexZeroItems() {
        handle.execute("INSERT INTO public.\"index\" (\"name\", \"key\", sha256hex, start_entry_number, end_entry_number, start_index_entry_number, end_index_entry_number)" +
                " VALUES('by-type', 'UA', 'xxx37d', 91, NULL, 91, NULL)");
        // change BBD from UA to MD
        handle.execute("UPDATE public.\"index\" set end_index_entry_number=92, end_entry_number=92 where name='by-type' and sha256hex='xxx37d'");

        handle.execute("INSERT INTO public.\"index\" (\"name\", \"key\", sha256hex, start_entry_number, end_entry_number, start_index_entry_number, end_index_entry_number)" +
                " VALUES('by-type', 'MD', 'xxx509', 92, NULL, 93, NULL)");
    }

}
