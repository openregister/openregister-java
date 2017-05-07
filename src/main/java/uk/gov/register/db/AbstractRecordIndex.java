package uk.gov.register.db;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Record;
import uk.gov.register.core.RecordIndex;
import uk.gov.register.store.DataAccessLayer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class AbstractRecordIndex implements RecordIndex {
    protected final DataAccessLayer dataAccessLayer;

    public AbstractRecordIndex(DataAccessLayer dataAccessLayer) {
        this.dataAccessLayer = dataAccessLayer;
    }

    @Override
    public Optional<Record> getRecord(String key) {
        return dataAccessLayer.getRecord(key);
    }

    @Override
    public int getTotalRecords() {
        return dataAccessLayer.getTotalRecords();
    }

    @Override
    public List<Record> getRecords(int limit, int offset) {
        return dataAccessLayer.getRecords(limit, offset);
    }

    @Override
    public List<Record> findMax100RecordsByKeyValue(String key, String value) {
        return dataAccessLayer.findMax100RecordsByKeyValue(key, value);
    }

    @Override
    public Collection<Entry> findAllEntriesOfRecordBy(String key) {
        return dataAccessLayer.findAllEntriesOfRecordBy(key);
    }
}
