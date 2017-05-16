package uk.gov.register.store.postgres;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.db.*;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;

public abstract class PostgresReadDataAccessLayer implements DataAccessLayer {
    private final EntryQueryDAO entryQueryDAO;
    private final IndexQueryDAO indexQueryDAO;
    private final ItemQueryDAO itemQueryDAO;
    private final RecordQueryDAO recordQueryDAO;

    public PostgresReadDataAccessLayer(
            EntryQueryDAO entryQueryDAO, IndexQueryDAO indexQueryDAO,
            ItemQueryDAO itemQueryDAO, RecordQueryDAO recordQueryDAO) {
        this.entryQueryDAO = entryQueryDAO;
        this.indexQueryDAO = indexQueryDAO;
        this.itemQueryDAO = itemQueryDAO;
        this.recordQueryDAO = recordQueryDAO;
    }

    // Entry Log

    @Override
    public Optional<Entry> getEntry(int entryNumber) {
        checkpoint();
        return entryQueryDAO.findByEntryNumber(entryNumber);
    }

    @Override
    public Collection<Entry> getEntries(int start, int limit) {
        checkpoint();
        return entryQueryDAO.getEntries(start, limit);
    }

    @Override
    public Collection<Entry> getAllEntries() {
        checkpoint();
        return entryQueryDAO.getAllEntriesNoPagination();
    }

    @Override
    public Iterator<Entry> getEntryIterator() {
        checkpoint();
        return entryQueryDAO.getIterator();
    }

    @Override
    public Iterator<Entry> getEntryIterator(int totalEntries1, int totalEntries2) {
        checkpoint();
        return entryQueryDAO.getIterator(totalEntries1, totalEntries2);
    }

    @Override
    public Iterator<Entry> getIndexEntryIterator(String indexName) {
        return indexQueryDAO.getIterator(indexName);
    }

    @Override
    public Iterator<Entry> getIndexEntryIterator(String indexName, int totalEntries1, int totalEntries2) {
        return indexQueryDAO.getIterator(indexName, totalEntries1, totalEntries2);
    }

    @Override
    public <R> R withEntryIterator(Function<EntryIterator, R> callback) {
        checkpoint();
        return EntryIterator.withEntryIterator(entryQueryDAO, entryIterator -> callback.apply(entryIterator));
    }

    @Override
    public int getTotalEntries() {
        checkpoint();
        return entryQueryDAO.getTotalEntries();
    }

    @Override
    public Optional<Instant> getLastUpdatedTime() {
        checkpoint();
        return entryQueryDAO.getLastUpdatedTime();
    }

    // Item Store

    @Override
    public Optional<Item> getItemBySha256(HashValue hash) {
        checkpoint();
        return itemQueryDAO.getItemBySHA256(hash.getValue());
    }

    @Override
    public Collection<Item> getAllItems() {
        checkpoint();
        return itemQueryDAO.getAllItemsNoPagination();
    }

    @Override
    public Iterator<Item> getItemIterator() {
        checkpoint();
        return itemQueryDAO.getIterator();
    }

    @Override
    public Iterator<Item> getItemIterator(int start, int end) {
        checkpoint();
        return itemQueryDAO.getIterator(start, end);
    }

    // Record Index

    @Override
    public Optional<Record> getRecord(String key) {
        checkpoint();
        return recordQueryDAO.findByPrimaryKey(key);
    }

    @Override
    public List<Record> getRecords(int limit, int offset) {
        checkpoint();
        return recordQueryDAO.getRecords(limit, offset);
    }

    @Override
    public List<Record> findMax100RecordsByKeyValue(String key, String value) {
        checkpoint();
        return recordQueryDAO.findMax100RecordsByKeyValue(key, value);
    }

    @Override
    public Collection<Entry> findAllEntriesOfRecordBy(String key) {
        checkpoint();
        return recordQueryDAO.findAllEntriesOfRecordBy(key);
    }

    @Override
    public int getTotalRecords() {
        checkpoint();
        return recordQueryDAO.getTotalRecords();
    }

    // Index

    @Override
    public Optional<Record> getIndexRecord(String key, String indexName) {
        Optional<Record> record = indexQueryDAO.findRecord(key, indexName);
        return record.filter(r -> r.getItems().size() != 0);
    }

    @Override
    public List<Record> getIndexRecords(int limit, int offset, String indexName) {
        return indexQueryDAO.findRecords(limit, offset, indexName);
    }

    @Override
    public int getTotalIndexRecords(String indexName) {
        return indexQueryDAO.getTotalRecords(indexName);
    }

    protected abstract void checkpoint();
}
