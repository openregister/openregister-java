package uk.gov.register.core;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RecordIndex {
    void updateRecordIndex(String key, Integer entryNumber);

    Optional<Record> getRecord(String key);

    int getTotalRecords();

    List<Record> getRecords(int limit, int offset);

    List<Record> findMax100RecordsByKeyValue(String key, String value);

    Collection<Entry> findAllEntriesOfRecordBy(String registerName, String key);

    void checkpoint();
}
