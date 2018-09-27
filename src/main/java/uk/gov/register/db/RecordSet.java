package uk.gov.register.db;

import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Record;
import uk.gov.register.store.DataAccessLayer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class RecordSet {
    private final DataAccessLayer dataAccessLayer;

    public RecordSet(DataAccessLayer dataAccessLayer) {
        this.dataAccessLayer = dataAccessLayer;
    }

    public Optional<Record> getRecord(EntryType entryType, String key) {
        Optional<Record> record = dataAccessLayer.getRecord(entryType, key);
        return record.filter(r -> r.getItems().size() != 0);
    }

    public List<Record> getRecords(EntryType entryType, int limit, int offset) {
        return dataAccessLayer.getRecords(entryType, limit, offset);
    }

    public int getTotalRecords(EntryType entryType) {
        return dataAccessLayer.getTotalRecords(entryType);
    }

    public List<Record> findMax100RecordsByKeyValue(String key, String value) {
        return dataAccessLayer.findMax100RecordsByKeyValue(EntryType.user, key, value);
    }

    public Collection<Entry> findAllEntriesOfRecordBy(String key) {
        return dataAccessLayer.getAllEntriesByKey(key);
    }
}
