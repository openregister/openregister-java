package uk.gov.register.store;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.util.HashValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public interface BackingStoreDriver {

    void insertItem(Item item);
    void insertRecord(Record record, String registerName);

    Optional<Item> getItemBySha256(HashValue hash);
    Collection<Item> getAllItems();

    Optional<Record> getRecord(String key);
    int getTotalRecords();
    List<Record> getRecords(int limit, int offset);
    List<Record> findMax100RecordsByKeyValue(String key, String value);
    Collection<Entry> findAllEntriesOfRecordBy(String registerName, String key);

    Iterator<Item> getItemIterator();
    Iterator<Item> getItemIterator(int start, int end);
}
