package uk.gov.register.store.postgres;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.db.*;
import uk.gov.register.indexer.IndexEntryNumberItemCountPair;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class PostgresReadDataAccessLayer implements DataAccessLayer {
    private final EntryQueryDAO entryQueryDAO;
    private final IndexQueryDAO indexQueryDAO;
    final ItemQueryDAO itemQueryDAO;
    private final RecordQueryDAO recordQueryDAO;
    final String schema;

    public PostgresReadDataAccessLayer(
            EntryQueryDAO entryQueryDAO, IndexQueryDAO indexQueryDAO,
            ItemQueryDAO itemQueryDAO, RecordQueryDAO recordQueryDAO, String schema) {
        this.entryQueryDAO = entryQueryDAO;
        this.indexQueryDAO = indexQueryDAO;
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
    public Iterator<Entry> getEntryIterator() {
        checkpoint();
        return entryQueryDAO.getIterator(schema);
    }

    @Override
    public Iterator<Entry> getEntryIterator(int totalEntries1, int totalEntries2) {
        checkpoint();
        return entryQueryDAO.getIterator(totalEntries1, totalEntries2, schema);
    }

    @Override
    public Iterator<Entry> getIndexEntryIterator(String indexName) {
        return indexQueryDAO.getIterator(indexName, schema, indexName.equals("metadata") ? "entry_system" : "entry");
    }

    @Override
    public Iterator<Entry> getIndexEntryIterator(String indexName, int totalEntries1, int totalEntries2) {
        return indexQueryDAO.getIterator(indexName, totalEntries1, totalEntries2, schema, indexName.equals("metadata") ? "entry_system" : "entry");
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
    public Optional<Item> getItemBySha256(HashValue hash) {
        return itemQueryDAO.getItemBySHA256(hash.getValue(), schema);
    }

    @Override
    public Collection<Item> getAllItems() {
        checkpoint();
        return itemQueryDAO.getAllItemsNoPagination(schema);
    }

    @Override
    public Iterator<Item> getItemIterator() {
        checkpoint();
        return itemQueryDAO.getIterator(schema);
    }

    @Override
    public Iterator<Item> getItemIterator(int start, int end) {
        checkpoint();
        return itemQueryDAO.getIterator(start, end, schema);
    }
    
    @Override
    public Iterator<Item> getSystemItemIterator() {
        checkpoint();
        return itemQueryDAO.getSystemItemIterator(schema);
    }

    // Record Index

    @Override
    public Optional<Record> getRecord(String key) {
        checkpoint();
        return recordQueryDAO.findByPrimaryKey(key, schema);
    }

    @Override
    public List<Record> getRecords(int limit, int offset) {
        checkpoint();
        return recordQueryDAO.getRecords(limit, offset, schema);
    }

    @Override
    public List<Record> findMax100RecordsByKeyValue(String key, String value) {
        checkpoint();
        return recordQueryDAO.findMax100RecordsByKeyValue(key, value, schema);
    }

    @Override
    public Collection<Entry> findAllEntriesOfRecordBy(String key) {
        checkpoint();
        return recordQueryDAO.findAllEntriesOfRecordBy(key, schema);
    }

    @Override
    public int getTotalRecords() {
        checkpoint();
        return recordQueryDAO.getTotalRecords(schema);
    }

    // Index

    @Override
    public Optional<Record> getIndexRecord(String key, String indexName) {
        return indexQueryDAO.findRecord(key, indexName, schema, indexName.equals("metadata") ? "entry_system" : "entry");
    }

    @Override
    public List<Record> getIndexRecords(int limit, int offset, String indexName) {
        return indexQueryDAO.findRecords(limit, offset, indexName, schema, indexName.equals("metadata") ? "entry_system" : "entry");
    }

    @Override
    public int getTotalIndexRecords(String indexName) {
        return indexQueryDAO.getTotalRecords(indexName, schema);
    }

    @Override
    public int getCurrentIndexEntryNumber(String indexName) {
        return indexQueryDAO.getCurrentIndexEntryNumber(indexName, schema);
    }

    @Override
    public IndexEntryNumberItemCountPair getStartIndexEntryNumberAndExistingItemCount(String indexName, String key, String sha256hex){
        return indexQueryDAO.getStartIndexEntryNumberAndExistingItemCount(indexName, key,sha256hex, schema);
    }

    protected abstract void checkpoint();
}
