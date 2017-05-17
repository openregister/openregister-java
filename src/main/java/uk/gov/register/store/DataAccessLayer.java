package uk.gov.register.store;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.db.EntryIterator;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface DataAccessLayer {

    // Entry Log
    void appendEntry(Entry entry);
    Optional<Entry> getEntry(int entryNumber);
    Collection<Entry> getEntries(int start, int limit);
    Collection<Entry> getAllEntries();
    Iterator<Entry> getEntryIterator();
    Iterator<Entry> getEntryIterator(int totalEntries1, int totalEntries2);
    Iterator<Entry> getIndexEntryIterator(String indexName);
    Iterator<Entry> getIndexEntryIterator(String indexName, int totalEntries1, int totalEntries2);
    <R> R withEntryIterator(Function<EntryIterator, R> callback);
    int getTotalEntries();
    Optional<Instant> getLastUpdatedTime();

    // Item Store
    void putItem(Item item);
    Optional<Item> getItemBySha256(HashValue hash);
    Collection<Item> getAllItems();
    Iterator<Item> getItemIterator();
    Iterator<Item> getItemIterator(int start, int end);

    // Record Index
    void updateRecordIndex(Entry entry);
    Optional<Record> getRecord(String key);
    List<Record> getRecords(int limit, int offset);
    List<Record> findMax100RecordsByKeyValue(String key, String value);
    Collection<Entry> findAllEntriesOfRecordBy(String key);
    int getTotalRecords();

    // Index
    Optional<Record> getIndexRecord(String key, String indexName);
    List<Record> getIndexRecords(int limit, int offset, String indexName);
    int getTotalIndexRecords(String indexName);
}
