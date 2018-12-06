package uk.gov.register.core;

import uk.gov.register.exceptions.IndexingException;
import uk.gov.register.store.DataAccessLayer;

import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

public class EntryLogImpl implements EntryLog {
    private final DataAccessLayer dataAccessLayer;

    public EntryLogImpl(DataAccessLayer dataAccessLayer) {
        this.dataAccessLayer = dataAccessLayer;
    }

    @Override
    public void appendEntry(Entry entry) throws IndexingException {
        Optional<Record> record = dataAccessLayer.getRecord(entry.getEntryType(), entry.getKey());

        if (record.isPresent() && record.get().getEntry().getV1ItemHash().equals(entry.getV1ItemHash())) {
            throw new IndexingException(entry, "Cannot contain identical items to previous entry");
        }

        dataAccessLayer.appendEntry(entry);
    }

    @Override
    public Optional<Entry> getEntry(int entryNumber) {
        return dataAccessLayer.getEntry(entryNumber);
    }

    @Override
    public Collection<Entry> getEntries(int start, int limit) {
        return dataAccessLayer.getEntries(start, limit);
    }

    @Override
    public Iterator<Entry> getEntryIterator(EntryType entryType) {
        return dataAccessLayer.getEntryIterator(entryType);
    }

    @Override
    public Iterator<Entry> getEntryIterator(EntryType entryType, int totalEntries1, int totalEntries2) {
        return dataAccessLayer.getEntryIterator(entryType, totalEntries1, totalEntries2);
    }

    @Override
    public Collection<Entry> getAllEntries() {
        return dataAccessLayer.getAllEntries();
    }

    @Override
    public int getTotalEntries(EntryType entryType) {
        return dataAccessLayer.getTotalEntries(entryType);
    }

    @Override
    public Optional<Instant> getLastUpdatedTime() {
        return dataAccessLayer.getLastUpdatedTime();
    }
}
