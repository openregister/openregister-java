package uk.gov.register.core;

import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;

import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

/**
 * An append-only log of Entries, together with proofs
 */
public interface EntryLog {
    void appendEntry(Entry entry);

    Optional<Entry> getEntry(int entryNumber);
    Collection<Entry> getEntries(int start, int limit);
    Collection<Entry> getAllEntries();

    Iterator<Entry> getEntryIterator(String indexName, int totalEntries1, int totalEntries2);
    Iterator<Entry> getEntryIterator(String indexName);

    int getTotalEntries();
    int getTotalEntries(EntryType entryType);

    Optional<Instant> getLastUpdatedTime();

    RegisterProof getRegisterProof();
    RegisterProof getRegisterProof(int totalEntries);

    EntryProof getEntryProof(int entryNumber, int totalEntries);

    ConsistencyProof getConsistencyProof(int totalEntries1, int totalEntries2);
}
