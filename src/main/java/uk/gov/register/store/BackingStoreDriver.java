package uk.gov.register.store;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BackingStoreDriver {

    void insertEntry(Entry entry);
    void insertItem(Item item);
    void insertRecord(Record record, String registerName);

    Optional<Entry> getEntry(int entryNumber);
    Collection<Entry> getEntries(int start, int limit);
    Collection<Entry> getAllEntries();
    int getTotalEntries();
    Optional<Instant> getLastUpdatedTime();

    Optional<Item> getItemBySha256(String sha256hex);
    Collection<Item> getAllItems();

    Optional<Record> getRecord(String key);
    int getTotalRecords();
    List<Record> getRecords(int limit, int offset);
    List<Record> findMax100RecordsByKeyValue(String key, String value);
    Collection<Entry> findAllEntriesOfRecordBy(String registerName, String key);

    RegisterProof getRegisterProof() throws NoSuchAlgorithmException;
    EntryProof getEntryProof(int entryNumber, int totalEntries);
    ConsistencyProof getConsistencyProof(int totalEntries1, int totalEntries2);
}
