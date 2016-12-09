package uk.gov.register.store;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Record;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BackingStoreDriver {

    void insertRecord(Record record, String registerName);

    Optional<Record> getRecord(String key);
    int getTotalRecords();
    List<Record> getRecords(int limit, int offset);
    List<Record> findMax100RecordsByKeyValue(String key, String value);
    Collection<Entry> findAllEntriesOfRecordBy(String registerName, String key);
}
