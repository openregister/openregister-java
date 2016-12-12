package uk.gov.register.db;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Record;
import uk.gov.register.core.RecordIndex;
import uk.gov.register.store.postgres.PostgresDriverNonTransactional;

import javax.inject.Inject;
import java.util.*;

public class OnDemandRecordIndex implements RecordIndex {
    private final PostgresDriverNonTransactional backingStoreDriver;

    @Inject
    public OnDemandRecordIndex(PostgresDriverNonTransactional backingStoreDriver) {
        this.backingStoreDriver = backingStoreDriver;
    }

    @Override
    public void updateRecordIndex(String key, Integer entryNumber) {
        backingStoreDriver.insertRecord(key, entryNumber);
    }

    @Override
    public Optional<Record> getRecord(String key) {
        return backingStoreDriver.getRecord(key);
    }

    @Override
    public int getTotalRecords() {
        return backingStoreDriver.getTotalRecords();
    }

    @Override
    public List<Record> getRecords(int limit, int offset) {
        return backingStoreDriver.getRecords(limit, offset);
    }

    @Override
    public List<Record> findMax100RecordsByKeyValue(String key, String value) {
        return backingStoreDriver.findMax100RecordsByKeyValue(key, value);
    }

    @Override
    public Collection<Entry> findAllEntriesOfRecordBy(String registerName, String key) {
        return backingStoreDriver.findAllEntriesOfRecordBy(registerName, key);
    }
}
