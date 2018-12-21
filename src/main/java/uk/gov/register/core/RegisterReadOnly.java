package uk.gov.register.core;

import uk.gov.register.exceptions.NoSuchFieldException;
import uk.gov.register.exceptions.NoSuchRegisterException;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;

import java.time.Instant;
import java.util.*;

public interface RegisterReadOnly {
    // Get using the old item hashes
    Optional<Item> getItemByV1Hash(HashValue hash);

    // Get using the new blob hashes
    Optional<Item> getItem(HashValue hash);

    Collection<Item> getAllItems();
    Collection<Item> getAllItems(EntryType entryType);
    Iterator<Item> getItemIterator(EntryType entryType);
    Iterator<Item> getItemIterator(int start, int end);
    Collection<Item> getUserItemsPaginated(Optional<HashValue> start, int limit);
    int getTotalItems();

    Optional<Entry> getEntry(int entryNumber);
    Collection<Entry> getEntries(int start, int limit);
    Collection<Entry> getAllEntries();
    Iterator<Entry> getEntryIterator(EntryType entryType);
    Iterator<Entry> getEntryIterator(int totalEntries1, int totalEntries2);
    Collection<Entry> allEntriesOfRecord(String key);
    int getTotalEntries(EntryType entryType);
    Optional<Instant> getLastUpdatedTime();

    Optional<Record> getRecord(EntryType entryType, String key);
    List<Record> getRecords(EntryType entryType, int limit, int offset);
    List<Record> max100RecordsFacetedByKeyValue(String key, String value) throws NoSuchFieldException;
    int getTotalRecords(EntryType entryType);

    RegisterId getRegisterId();
    Optional<String> getRegisterName();
    Optional<String> getCustodianName();

    RegisterMetadata getRegisterMetadata() throws NoSuchRegisterException;

    Map<String, Field> getFieldsByName() throws NoSuchRegisterException, NoSuchFieldException;
}

