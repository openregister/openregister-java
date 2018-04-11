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

    // Items
    void addItem(Item item);
    Optional<Item> getItem(HashValue hash);
    Collection<Item> getAllItems();
    Iterator<Item> getItemIterator(EntryType entryType);
    Iterator<Item> getItemIterator(int startEntryNumber, int endEntryNumber);

    // Entries
    void appendEntry(Entry entry);
    Optional<Entry> getEntry(int entryNumber);
    Collection<Entry> getEntries(int start, int limit);
    Collection<Entry> getAllEntries();

    Iterator<Entry> getEntryIterator(String indexName);
    Iterator<Entry> getEntryIterator(String indexName, int totalEntries1, int totalEntries2);
    <R> R withEntryIterator(Function<EntryIterator, R> callback);
    int getTotalEntries();
    int getTotalEntries(EntryType entryType);

    Optional<Instant> getLastUpdatedTime();

    // Indexes
    void start(String indexName, String key, String itemHash, int startEntryNumber, int startIndexEntryNumber);
    void end(String indexName, String entryKey, String indexKey, String itemHash, int endEntryNumber, int endIndexEntryNumber, int entryNumberToEnd);
    Optional<Record> getRecord(String key, String indexName);
    List<Record> getRecords(int limit, int offset, String indexName);
    int getTotalRecords(String indexName);
    int getCurrentIndexEntryNumber(String indexName);

    List<Record> findMax100RecordsByKeyValue(String key, String value);
    Collection<Entry> getAllEntriesByKey(String key);

    IndexEntryNumberItemCountPair getStartIndexEntryNumberAndExistingItemCount(String indexName, String key, String sha256hex);
}
