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
    Optional<Blob> getBlob(HashValue hash);
    Collection<Blob> getAllBlobs();

    Optional<BaseEntry> getEntry(int entryNumber);
    Collection<BaseEntry> getEntries(int start, int limit);
    Collection<BaseEntry> getAllEntries();
    int getTotalEntries();
    int getTotalEntries(EntryType entryType);

    Optional<Instant> getLastUpdatedTime();

    Optional<Record> getRecord(String key);
    int getTotalRecords();
    Collection<BaseEntry> allEntriesOfRecord(String key);
    List<Record> getRecords(int limit, int offset);

    List<Record> max100RecordsFacetedByKeyValue(String key, String value) throws NoSuchFieldException;

    RegisterProof getRegisterProof();
    RegisterProof getRegisterProof(int entryNo);
    EntryProof getEntryProof(int entryNumber, int totalEntries);
    ConsistencyProof getConsistencyProof(int totalEntries1, int totalEntries2);

    Iterator<BaseEntry> getEntryIterator();
    Iterator<BaseEntry> getEntryIterator(int totalEntries1, int totalEntries2);

    Iterator<Blob> getBlobIterator();
    Iterator<Blob> getBlobIterator(int start, int end);
    Iterator<Blob> getSystemBlobIterator();
    
    Iterator<BaseEntry> getEntryIterator(String indexName);
    Iterator<BaseEntry> getEntryIterator(String indexName, int totalEntries1, int totalEntries2);

    RegisterId getRegisterId();
    Optional<String> getRegisterName();
    Optional<String> getCustodianName();

    RegisterMetadata getRegisterMetadata() throws NoSuchRegisterException;

    Optional<Record> getRecord(String key, String derivationName);

    List<Record> getRecords(int limit, int offset, String derivationName);

    int getTotalRecords(String derivationName);

    Map<String, Field> getFieldsByName() throws NoSuchRegisterException, NoSuchFieldException;
}

