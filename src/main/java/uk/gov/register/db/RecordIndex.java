package uk.gov.register.db;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Record;
import uk.gov.register.store.BackingStoreDriver;

import java.util.*;
import java.util.stream.Collectors;

public class RecordIndex {
    private final BackingStoreDriver backingStoreDriver;

    public RecordIndex(BackingStoreDriver backingStoreDriver) {
        this.backingStoreDriver = backingStoreDriver;
    }

    public void updateRecordIndex(String registerName, Record record) {
        backingStoreDriver.insertRecord(record, registerName);
    }

    public Optional<Record> getRecord(String key) {
        return backingStoreDriver.getRecord(key);
    }

    public int getTotalRecords() {
        return backingStoreDriver.getTotalRecords();
    }

    public List<Record> getRecords(int limit, int offset) {
        return backingStoreDriver.getRecords(limit, offset);
    }

    public List<Record> findMax100RecordsByKeyValue(String key, String value) {
        return backingStoreDriver.findMax100RecordsByKeyValue(key, value);
    }

    public Collection<Entry> findAllEntriesOfRecordBy(String registerName, String key) {
        return backingStoreDriver.findAllEntriesOfRecordBy(registerName, key);
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
