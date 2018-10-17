package uk.gov.register.db;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Record;
import uk.gov.register.store.DataAccessLayer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Index {
    private final DataAccessLayer dataAccessLayer;

    public Index(DataAccessLayer dataAccessLayer) {
        this.dataAccessLayer = dataAccessLayer;
    }

    public Optional<Record> getRecord(String key, String derivationName) {
        Optional<Record> record = dataAccessLayer.getRecord(key, derivationName);
        return record.filter(r -> r.getBlobs().size() != 0);
    }

    public List<Record> getRecords(int limit, int offset, String derivationName) {
        return dataAccessLayer.getRecords(limit, offset, derivationName);
    }

    public int getTotalRecords(String derivationName) {
        return dataAccessLayer.getTotalRecords(derivationName);
    }

    public List<Record> findMax100RecordsByKeyValue(String key, String value) {
        return dataAccessLayer.findMax100RecordsByKeyValue(key, value);
    }

    public Collection<Entry> findAllEntriesOfRecordBy(String key) {
        return dataAccessLayer.getAllEntriesByKey(key);
    }
}
