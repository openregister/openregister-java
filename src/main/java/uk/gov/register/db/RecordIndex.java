package uk.gov.register.db;

import com.google.common.collect.Lists;
import org.skife.jdbi.v2.Handle;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Record;

import java.util.*;
import java.util.stream.Collectors;

public class RecordIndex {
    public void updateRecordIndex(Handle handle, String registerName, List<Record> records) {
        CurrentKeysUpdateDAO currentKeysUpdateDAO = handle.attach(CurrentKeysUpdateDAO.class);
        List<CurrentKey> currentKeys = extractCurrentKeys(registerName, records);
        int noOfRecordsDeleted = currentKeysUpdateDAO.removeRecordWithKeys(Lists.transform(currentKeys, r -> r.key));

        currentKeysUpdateDAO.writeCurrentKeys(currentKeys);
        currentKeysUpdateDAO.updateTotalRecords(currentKeys.size() - noOfRecordsDeleted);
    }

    public Optional<Record> getRecord(Handle h, String key) {
        return h.attach(RecordQueryDAO.class).findByPrimaryKey(key);
    }

    public int getTotalRecords(Handle h) {
        return h.attach(RecordQueryDAO.class).getTotalRecords();
    }

    public List<Record> getRecords(Handle h, int limit, int offset) {
        return h.attach(RecordQueryDAO.class).getRecords(limit, offset);
    }

    public List<Record> findMax100RecordsByKeyValue(Handle h, String key, String value) {
        return h.attach(RecordQueryDAO.class).findMax100RecordsByKeyValue(key, value);
    }

    public Collection<Entry> findAllEntriesOfRecordBy(Handle h, String registerName, String key) {
        return h.attach(RecordQueryDAO.class).findAllEntriesOfRecordBy(registerName, key);
    }

    private List<CurrentKey> extractCurrentKeys(String registerName, List<Record> records) {
        Map<String, Integer> currentKeys = new HashMap<>();
        records.forEach(r -> currentKeys.put(r.item.getKey(registerName), r.entry.getEntryNumber()));
        return currentKeys
                .entrySet()
                .stream()
                .map(e -> new CurrentKey(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }
}
