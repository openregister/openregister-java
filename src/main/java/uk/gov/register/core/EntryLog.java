package uk.gov.register.core;

import uk.gov.register.exceptions.IndexingException;
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
    void appendEntry(Entry entry) throws IndexingException;

    Optional<Entry> getEntry(int entryNumber);
    Collection<Entry> getEntries(int start, int limit);
    Collection<Entry> getAllEntries();

    Iterator<Entry> getEntryIterator(EntryType entryType, int totalEntries1, int totalEntries2);
    Iterator<Entry> getEntryIterator(EntryType entryType);

    int getTotalEntries(EntryType entryType);

    Optional<Instant> getLastUpdatedTime();

    RegisterProof getV1RegisterProof();
    RegisterProof getV1RegisterProof(int totalEntries);

    EntryProof getV1EntryProof(int entryNumber, int totalEntries);

    ConsistencyProof getV1ConsistencyProof(int totalEntries1, int totalEntries2);
}
