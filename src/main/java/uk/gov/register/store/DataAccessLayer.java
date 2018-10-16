package uk.gov.register.store;

import uk.gov.register.core.Blob;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Record;
import uk.gov.register.db.EntryIterator;
import uk.gov.register.exceptions.IndexingException;
import uk.gov.register.indexer.IndexEntryNumberItemCountPair;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;

public interface DataAccessLayer {

    // Blobs
    void addBlob(Blob blob);
    Optional<Blob> getBlob(HashValue hash);
    Collection<Blob> getAllBlobs();
    Iterator<Blob> getBlobIterator(EntryType entryType);
    Iterator<Blob> getBlobIterator(int startEntryNumber, int endEntryNumber);

    // Entries
    void appendEntry(Entry entry) throws IndexingException;
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
    void start(String indexName, String key, String blobHash, int startEntryNumber, int startIndexEntryNumber);
    void end(String indexName, String entryKey, String indexKey, String blobHash, int endEntryNumber, int endIndexEntryNumber, int entryNumberToEnd);
    Optional<Record> getRecord(String key, String indexName);
    List<Record> getRecords(int limit, int offset, String indexName);
    int getTotalRecords(String indexName);
    int getCurrentIndexEntryNumber(String indexName);

    List<Record> findMax100RecordsByKeyValue(String key, String value);
    Collection<Entry> getAllEntriesByKey(String key);

    IndexEntryNumberItemCountPair getStartIndexEntryNumberAndExistingBlobCount(String indexName, String key, String sha256hex);
}
