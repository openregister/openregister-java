package uk.gov.register.store.postgres;

import uk.gov.register.configuration.IndexFunctionConfiguration.IndexNames;
import uk.gov.register.core.Blob;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Record;
import uk.gov.register.db.EntryIterator;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.db.IndexQueryDAO;
import uk.gov.register.db.BlobQueryDAO;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;

public abstract class PostgresReadDataAccessLayer implements DataAccessLayer {
    protected final EntryQueryDAO entryQueryDAO;
    private final BlobQueryDAO blobQueryDAO;
    protected final IndexQueryDAO indexQueryDAO;
    protected final String schema;
    
    public PostgresReadDataAccessLayer(
            EntryQueryDAO entryQueryDAO, IndexQueryDAO indexQueryDAO,
            BlobQueryDAO blobQueryDAO, String schema) {
        this.entryQueryDAO = entryQueryDAO;
        this.indexQueryDAO = indexQueryDAO;
        this.blobQueryDAO = blobQueryDAO;
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
    public Iterator<Entry> getEntryIterator(String indexName) {
    // TODO: Remove if statement and use indexQueryDAO for RECORD index. This can only be done once the government-service
    // register no longer contains duplicate consecutive entries. Else we should not make the assumption that duplicate
    // consecutive entries do not exist.
        if (indexName.equals(IndexNames.RECORD)) {
            return entryQueryDAO.getIterator(schema);
        }
        return indexQueryDAO.getIterator(indexName, schema, indexName.equals(IndexNames.METADATA) ? "entry_system" : "entry");
    }

    @Override
    public Iterator<Entry> getEntryIterator(String indexName, int totalEntries1, int totalEntries2) {
        // TODO: Remove if statement and use indexQueryDAO for RECORD index. This can only be done once the government-service
        // register no longer contains duplicate consecutive entries. Else we should not make the assumption that duplicate
        // consecutive entries do not exist.
        if (indexName.equals(IndexNames.RECORD)) {
            return entryQueryDAO.getIterator(totalEntries1, totalEntries2, schema);
        }
        return indexQueryDAO.getIterator(indexName, totalEntries1, totalEntries2, schema, indexName.equals(IndexNames.METADATA) ? "entry_system" : "entry");
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

    // Blob Store

    @Override
    public Optional<Blob> getBlob(HashValue hash) {
        return blobQueryDAO.getBlobBySHA256(hash.getValue(), schema);
    }

    @Override
    public Collection<Blob> getAllBlobs() {
        checkpoint();
        return blobQueryDAO.getAllBlobsNoPagination(schema);
    }

    @Override
    public Iterator<Blob> getBlobIterator(EntryType entryType) {
        checkpoint();

        switch (entryType) {
            case user: return blobQueryDAO.getIterator(schema);
            case system: return blobQueryDAO.getSystemBlobIterator(schema);
        }
        return blobQueryDAO.getIterator(schema);
    }

    @Override
    public Iterator<Blob> getBlobIterator(int startEntryNumber, int endEntryNumber) {
        checkpoint();
        return blobQueryDAO.getIterator(startEntryNumber, endEntryNumber, schema);
    }

    // Record Index

    @Override
    public List<Record> findMax100RecordsByKeyValue(String key, String value) {
        checkpoint();
        return indexQueryDAO.findMax100RecordsByKeyValue(key, value, schema);
    }

    @Override
    public Collection<Entry> getAllEntriesByKey(String key) {
        checkpoint();
        return entryQueryDAO.getAllEntriesByKey(key, schema);
    }

    // Index
    @Override
    public Optional<Record> getRecord(String key, String indexName) {
        List<Record> records = getIndexRecords(Arrays.asList(key), indexName);
        
        return records.size() == 1 ? Optional.of(records.get(0)) : Optional.empty();
    }
    
    protected List<Record> getIndexRecords(List<String> keys, String indexName) {
        checkpoint();
        return indexQueryDAO.findRecords(keys, indexName, schema, indexName.equals(IndexNames.METADATA) ? "entry_system" : "entry");
    }

    @Override
    public List<Record> getRecords(int limit, int offset, String indexName) {
        checkpoint();
        return indexQueryDAO.findRecords(limit, offset, indexName, schema, indexName.equals(IndexNames.METADATA) ? "entry_system" : "entry");
    }

    @Override
    public int getTotalRecords(String indexName) {
        return indexQueryDAO.getTotalRecords(indexName, schema);
    }

    @Override
    public int getCurrentIndexEntryNumber(String indexName) {
        return indexQueryDAO.getCurrentIndexEntryNumber(indexName, schema);
    }

    protected abstract void checkpoint();
}
