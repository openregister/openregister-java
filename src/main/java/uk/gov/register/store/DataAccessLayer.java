package uk.gov.register.store;

import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.proofs.EntryIterator;
import uk.gov.register.exceptions.IndexingException;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;

public interface DataAccessLayer {

    // Items
    void addItem(Item item);
    Optional<Item> getItemByV1Hash(HashValue hash);
    Optional<Item> getItem(HashValue hash);
    Collection<Item> getUserItemsPaginated(Optional<HashValue> start, int limit);
    Collection<Item> getAllItems();
    Collection<Item> getAllItems(EntryType entryType);
    Iterator<Item> getItemIterator(EntryType entryType);
    Iterator<Item> getItemIterator(int startEntryNumber, int endEntryNumber);

    // Entries
    void appendEntry(Entry entry) throws IndexingException;
    Optional<Entry> getEntry(int entryNumber);
    Collection<Entry> getEntries(int start, int limit);
    Collection<Entry> getAllEntries();
    Collection<Entry> getAllEntriesByKey(String key);
    Iterator<Entry> getEntryIterator(EntryType entryType);
    Iterator<Entry> getEntryIterator(EntryType entryType, int totalEntries1, int totalEntries2);
    <R> R withEntryIterator(Function<EntryIterator, R> callback);
    int getTotalEntries(EntryType entryType);

    Optional<Instant> getLastUpdatedTime();

    // Records
    Optional<Record> getRecord(EntryType entryType, String key);
    List<Record> getRecords(EntryType entryType, int limit, int offset);
    List<Record> findRecordsByKeyValue(EntryType entryType, String key, String value, Integer limit, Integer offset);
    int getTotalRecords(EntryType entryType);
}
