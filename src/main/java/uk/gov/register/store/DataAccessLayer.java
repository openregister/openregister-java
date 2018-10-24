package uk.gov.register.store;

import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.db.EntryIterator;
import uk.gov.register.exceptions.IndexingException;
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
    List<Record> findMax100RecordsByKeyValue(EntryType entryType, String key, String value);
    int getTotalRecords(EntryType entryType);
}
