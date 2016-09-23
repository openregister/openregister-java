package uk.gov.register.core;

import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RegisterReadOnly {
    Optional<Entry> getEntry(int entryNumber);

    Optional<Item> getItemBySha256(String sha256hex);

    int getTotalEntries();

    Collection<Entry> getEntries(int start, int limit);

    Optional<Record> getRecord(String key); // TODO: is "record" the right word?

    Collection<Entry> getAllEntries();

    Collection<Item> getAllItems();

    int getTotalRecords();
    Collection<Entry> allEntriesOfRecord(String key);
    List<Record> getRecords(int limit, int offset);

    List<Record> max100RecordsFacetedByKeyValue(String key, String value);

    Optional<Instant> getLastUpdatedTime();

    RegisterProof getRegisterProof() throws NoSuchAlgorithmException;

    EntryProof getEntryProof(int entryNumber, int totalEntries);

    ConsistencyProof getConsistencyProof(int totalEntries1, int totalEntries2);
}
