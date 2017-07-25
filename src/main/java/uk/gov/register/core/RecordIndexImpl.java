package uk.gov.register.core;

import uk.gov.register.store.DataAccessLayer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class RecordIndexImpl implements RecordIndex {
    protected final DataAccessLayer dataAccessLayer;

    public RecordIndexImpl(DataAccessLayer dataAccessLayer) {
        this.dataAccessLayer = dataAccessLayer;
    }

    @Override
    public void updateRecordIndex(Entry entry) {
        dataAccessLayer.updateRecordIndex(entry);
    }

    @Override
    public Optional<Record> getRecord(String key) {
        return dataAccessLayer.getRecord(key);
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
        return dataAccessLayer.getAllEntriesByKey(key);
    }
}
