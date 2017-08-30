package uk.gov.register.core;

import uk.gov.register.util.HashValue;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;

import java.time.Instant;
import java.util.*;

public interface RegisterReadOnly {
    Optional<Item> getItemBySha256(HashValue hash);
    Collection<Item> getAllItems();

    Optional<Entry> getEntry(int entryNumber);
    Collection<Entry> getEntries(int start, int limit);
    Collection<Entry> getAllEntries();
    int getTotalEntries();
    int getTotalEntries(EntryType entryType);

    Optional<Instant> getLastUpdatedTime();

    Optional<Record> getRecord(String key);
    int getTotalRecords();
    Collection<Entry> allEntriesOfRecord(String key);
    List<Record> getRecords(int limit, int offset);

    List<Record> max100RecordsFacetedByKeyValue(String key, String value);

    RegisterProof getRegisterProof();
    RegisterProof getRegisterProof(int entryNo);
    EntryProof getEntryProof(int entryNumber, int totalEntries);
    ConsistencyProof getConsistencyProof(int totalEntries1, int totalEntries2);

    Iterator<Entry> getEntryIterator();
    Iterator<Entry> getEntryIterator(int totalEntries1, int totalEntries2);

    Iterator<Item> getItemIterator();
    Iterator<Item> getItemIterator(int start, int end);
    Iterator<Item> getSystemItemIterator();
    
    Iterator<Entry> getDerivationEntryIterator(String indexName);
    Iterator<Entry> getDerivationEntryIterator(String indexName, int totalEntries1, int totalEntries2);

    RegisterName getRegisterName();
    Optional<String> getCustodianName();

    RegisterMetadata getRegisterMetadata();

    Optional<Record> getDerivationRecord(String key, String derivationName);

    List<Record> getDerivationRecords(int limit, int offset, String derivationName);

    int getTotalDerivationRecords(String derivationName);

    Map<String, Field> getRegisterFieldsByName();
    
    Map<String, Field> getMetadataFieldsByName();
}

