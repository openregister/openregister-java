package uk.gov.register.store;

import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.db.EntryIterator;
import uk.gov.register.indexer.IndexEntryNumberItemCountPair;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.*;
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
    int getTotalEntries(EntryType entryType);

    Optional<Instant> getLastUpdatedTime();

    // Item Store
    void putItem(Item item);
    Optional<Item> getItemBySha256(HashValue hash);
    Collection<Item> getAllItems();

    Iterator<Item> getItemIterator();
    Iterator<Item> getItemIterator(int start, int end);
    Iterator<Item> getSystemItemIterator();
    
    // Record Index
    Optional<Record> getRecord(String key);
    List<Record> getRecords(int limit, int offset);
    List<Record> findMax100RecordsByKeyValue(String key, String value);
    Collection<Entry> getAllEntriesByKey(String key);
    
    // Index
    void start(String indexName, String key, String itemHash, int startEntryNumber, int startIndexEntryNumber);
    void end(String indexName, String entryKey, String indexKey, String itemHash, int endEntryNumber, int endIndexEntryNumber, int entryNumberToEnd);
    Optional<Record> getIndexRecord(String key, String indexName);
    List<Record> getIndexRecords(int limit, int offset, String indexName);
    int getTotalIndexRecords(String indexName);
    int getCurrentIndexEntryNumber(String indexName);

    IndexEntryNumberItemCountPair getStartIndexEntryNumberAndExistingItemCount(String indexName, String key, String sha256hex);
}
