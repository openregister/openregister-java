package uk.gov.register.functional.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jdbi.OptionalContainerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.slf4j.MDC;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.db.EntryDAO;
import uk.gov.register.db.ItemDAO;
import uk.gov.register.db.RecordQueryDAO;
import uk.gov.register.functional.app.WipeDatabaseRule;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.runners.Parameterized.*;
import static uk.gov.register.core.HashingAlgorithm.*;
import static uk.gov.register.functional.app.TestRegister.address;

@RunWith(Parameterized.class)
public class RecordQueryFunctionalTest {
    private DBI dbi;
    private  Handle handle;
    private final String schema = address.getSchema();

    private RecordQueryDAO recordQueryDAO;
    private EntryDAO entryDAO;
    private ItemDAO itemDAO;

    private ObjectMapper objectMapper = new ObjectMapper();
    private Instant timestamp = Instant.ofEpochMilli(1490610633L * 1000L);

    private List<Entry> entries;
    private List<Item> items;

    @Parameter
    public String entryTable;

    @Parameters
    public static Collection<String> data() {
        return Arrays.asList("entry", "entry_system");
    }

    @Rule
    public WipeDatabaseRule wipeDatabaseRule = new WipeDatabaseRule(address);

    @Before
    public void setup() throws IOException {
        MDC.put("register", schema);
        dbi = new DBI(address.getDatabaseConnectionString("PGRegisterTxnFT"));
        dbi.registerContainerFactory(new OptionalContainerFactory());
        handle = dbi.open();

        recordQueryDAO = handle.attach(RecordQueryDAO.class);
        entryDAO = handle.attach(EntryDAO.class);
        itemDAO = handle.attach(ItemDAO.class);

        entries = Arrays.asList(
                new Entry(1, new HashValue(SHA256, "item1"), new HashValue(SHA256, "item1-blob-hash"), timestamp, "key1", EntryType.user),
                new Entry(2, new HashValue(SHA256, "item2"), new HashValue(SHA256, "item2-blob-hash"), timestamp, "key2", EntryType.user),
                new Entry(3, new HashValue(SHA256, "item3"), new HashValue(SHA256, "item3-blob-hash"), timestamp, "key1", EntryType.user),
                new Entry(4, new HashValue(SHA256, "item4"), new HashValue(SHA256, "item4-blob-hash"), timestamp, "key3", EntryType.user));

        items = Arrays.asList(
                new Item(new HashValue(SHA256, "item1"), new HashValue(SHA256, "item1-blob-hash"), objectMapper.readTree("{\"field1\":\"value1\",\"field2\":\"valueA\"}")),
                new Item(new HashValue(SHA256, "item2"), new HashValue(SHA256, "item2-blob-hash"), objectMapper.readTree("{\"field1\":\"value2\",\"field2\":\"valueB\"}")),
                new Item(new HashValue(SHA256, "item3"), new HashValue(SHA256, "item3-blob-hash"), objectMapper.readTree("{\"field1\":\"value3\",\"field2\":\"valueC\"}")),
                new Item(new HashValue(SHA256, "item4"), new HashValue(SHA256, "item4-blob-hash"), objectMapper.readTree("{\"field1\":\"value4\",\"field2\":\"valueC\"}")));
    }

    @After
    public void tearDown() {
        dbi.close(handle);
    }

    @Test
    public void shouldGetRecords() {
        insertEntriesAndItems(entryTable);

        List<Record> records = new ArrayList<>(recordQueryDAO.getRecords(100, 0, schema, entryTable));

        assertThat(records.size(), is(3));
        assertThat(records.get(0).getEntry(), is(entries.get(3)));
        assertThat(records.get(0).getItem(), is(items.get(3)));
        assertThat(records.get(1).getEntry(), is(entries.get(2)));
        assertThat(records.get(1).getItem(), is(items.get(2)));
        assertThat(records.get(2).getEntry(), is(entries.get(1)));
        assertThat(records.get(2).getItem(), is(items.get(1)));
    }

    @Test
    public void shouldGetRecord() {
        insertEntriesAndItems(entryTable);

        Optional<Record> record = recordQueryDAO.getRecord("key1", schema, entryTable);
        assertTrue(record.isPresent());
        assertThat(record.get().getEntry(), is(entries.get(2)));
        assertThat(record.get().getItem(), is(items.get(2)));

        record = recordQueryDAO.getRecord("key2", schema, entryTable);
        assertTrue(record.isPresent());
        assertThat(record.get().getEntry(), is(entries.get(1)));
        assertThat(record.get().getItem(), is(items.get(1)));

        record = recordQueryDAO.getRecord("key3", schema, entryTable);
        assertTrue(record.isPresent());
        assertThat(record.get().getEntry(), is(entries.get(3)));
        assertThat(record.get().getItem(), is(items.get(3)));

        record = recordQueryDAO.getRecord("key4", schema, entryTable);
        assertFalse(record.isPresent());
    }

    @Test
    public void shouldGetTotalRecords() {
        insertEntriesAndItems(entryTable);
        int totalRecords = recordQueryDAO.getTotalRecords(schema, entryTable);

        assertThat(totalRecords, is(3));
    }

    @Test
    public void shouldGetRecordFacets() {
        insertEntriesAndItems(entryTable);

        List<Record> records = new ArrayList<>(recordQueryDAO.findMax100RecordsByKeyValue("field2", "valueA", schema, entryTable));
        assertThat(records.size(), is(0));

        records = new ArrayList<>(recordQueryDAO.findMax100RecordsByKeyValue("field2", "valueB", schema, entryTable));
        assertThat(records.size(), is(1));
        assertThat(records.get(0).getEntry(), is(entries.get(1)));
        assertThat(records.get(0).getItem(), is(items.get(1)));

        records = new ArrayList<>(recordQueryDAO.findMax100RecordsByKeyValue("field2", "valueC", schema, entryTable));
        assertThat(records.size(), is(2));
        assertThat(records.get(0).getEntry(), is(entries.get(3)));
        assertThat(records.get(0).getItem(), is(items.get(3)));
        assertThat(records.get(1).getEntry(), is(entries.get(2)));
        assertThat(records.get(1).getItem(), is(items.get(2)));
    }

    private void insertEntriesAndItems(String entryTable) {
        entryDAO.insertInBatch(entries, schema, entryTable);
        itemDAO.insertInBatch(items, schema);
    }
}
