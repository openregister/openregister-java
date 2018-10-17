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
    void appendEntry(BaseEntry entry) throws IndexingException;

    Optional<BaseEntry> getEntry(int entryNumber);
    Collection<BaseEntry> getEntries(int start, int limit);
    Collection<BaseEntry> getAllEntries();

    Iterator<BaseEntry> getEntryIterator(String indexName, int totalEntries1, int totalEntries2);
    Iterator<BaseEntry> getEntryIterator(String indexName);

    int getTotalEntries();
    int getTotalEntries(EntryType entryType);

    Optional<Instant> getLastUpdatedTime();

    RegisterProof getRegisterProof();
    RegisterProof getRegisterProof(int totalEntries);

    EntryProof getEntryProof(int entryNumber, int totalEntries);

    ConsistencyProof getConsistencyProof(int totalEntries1, int totalEntries2);
}
