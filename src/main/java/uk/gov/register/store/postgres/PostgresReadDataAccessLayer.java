package uk.gov.register.store.postgres;

import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.db.EntryIterator;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.db.ItemQueryDAO;
import uk.gov.register.db.RecordQueryDAO;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;

public abstract class PostgresReadDataAccessLayer implements DataAccessLayer {
    protected final EntryQueryDAO entryQueryDAO;
    private final ItemQueryDAO itemQueryDAO;
    private final RecordQueryDAO recordQueryDAO;
    protected final String schema;
    
    public PostgresReadDataAccessLayer(
            EntryQueryDAO entryQueryDAO, ItemQueryDAO itemQueryDAO, RecordQueryDAO recordQueryDAO, String schema) {
        this.entryQueryDAO = entryQueryDAO;
        this.itemQueryDAO = itemQueryDAO;
        this.recordQueryDAO = recordQueryDAO;
        this.schema = schema;
    }

    // Entry Log

    @Override
    public Optional<Entry> getEntry(int entryNumber) {
        checkpoint();
        return entryQueryDAO.findByEntryNumber(entryNumber, schema);
    }

    @Override
    public Collection<Entry> getEntries(int start, int limit) {
        checkpoint();
        return entryQueryDAO.getEntries(start, limit, schema);
    }

    @Override
    public Collection<Entry> getAllEntries() {
        checkpoint();
        return entryQueryDAO.getAllEntriesNoPagination(schema);
    }

    @Override
    public Collection<Entry> getAllEntriesByKey(String key) {
        checkpoint();
        return entryQueryDAO.getAllEntriesByKey(key, schema);
    }

    @Override
    public Iterator<Entry> getEntryIterator(EntryType entryType) {
        return entryQueryDAO.getIterator(schema, getEntryTable(entryType), getEntryItemTable(entryType));
    }

    @Override
    public Iterator<Entry> getEntryIterator(EntryType entryType, int totalEntries1, int totalEntries2) {
        return entryQueryDAO.getIterator(totalEntries1, totalEntries2, schema, getEntryTable(entryType), getEntryItemTable(entryType));
    }

    @Override
    public <R> R withEntryIterator(Function<EntryIterator, R> callback) {
        checkpoint();
        return EntryIterator.withEntryIterator(entryQueryDAO, entryIterator -> callback.apply(entryIterator), schema);
    }

    @Override
    public int getTotalEntries() {
        return entryQueryDAO.getTotalEntries(schema);
    }

    public int getTotalSystemEntries() {
        checkpoint();
        return entryQueryDAO.getTotalSystemEntries(schema);
    }

    @Override
    public Optional<Instant> getLastUpdatedTime() {
        checkpoint();
        return entryQueryDAO.getLastUpdatedTime(schema);
    }

    // Item Store

    @Override
    public Optional<Item> getItem(HashValue hash) {
        return itemQueryDAO.getItemBySHA256(hash.getValue(), schema);
    }

    @Override
    public Collection<Item> getAllItems() {
        checkpoint();
        return itemQueryDAO.getAllItemsNoPagination(schema);
    }

    @Override
    public Iterator<Item> getItemIterator(EntryType entryType) {
        checkpoint();

        switch (entryType) {
            case user: return itemQueryDAO.getIterator(schema);
            case system: return itemQueryDAO.getSystemItemIterator(schema);
        }
        return itemQueryDAO.getIterator(schema);
    }

    @Override
    public Iterator<Item> getItemIterator(int startEntryNumber, int endEntryNumber) {
        checkpoint();
        return itemQueryDAO.getIterator(startEntryNumber, endEntryNumber, schema);
    }

    // Records

    @Override
    public Optional<Record> getRecord(EntryType entryType, String key) {
        checkpoint();
        return recordQueryDAO.getRecord(key, schema, getEntryTable(entryType), getEntryItemTable(entryType));
    }

    @Override
    public List<Record> getRecords(EntryType entryType, int limit, int offset) {
        checkpoint();
        return new ArrayList<>(recordQueryDAO.getRecords(limit, offset, schema, getEntryTable(entryType), getEntryItemTable(entryType)));
    }

    @Override
    public int getTotalRecords(EntryType entryType) {
        return recordQueryDAO.getTotalRecords(schema, getEntryTable(entryType));
    }

    @Override
    public List<Record> findMax100RecordsByKeyValue(EntryType entryType, String key, String value) {
        checkpoint();
        return new ArrayList<>(recordQueryDAO.findMax100RecordsByKeyValue(key, value, schema, getEntryTable(entryType), getEntryItemTable(entryType)));
    }

    protected abstract void checkpoint();

    private String getEntryTable(EntryType entryType) {
        return entryType.equals(EntryType.user) ? "entry" : "entry_system";
    }

    private String getEntryItemTable(EntryType entryType) {
        return entryType.equals(EntryType.user) ? "entry_item" : "entry_item_system";
    }
}
