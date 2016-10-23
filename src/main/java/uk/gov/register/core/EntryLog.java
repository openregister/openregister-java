package uk.gov.register.core;

import org.skife.jdbi.v2.Handle;
import uk.gov.register.db.EntryDAO;
import uk.gov.register.store.BackingStoreDriver;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;

import javax.inject.Inject;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * An append-only log of Entries, together with proofs
 */
public class EntryLog {
    private final BackingStoreDriver backingStoreDriver;

    @Inject
    public EntryLog(BackingStoreDriver backingStoreDriver) {
        this.backingStoreDriver = backingStoreDriver;
    }

    public void appendEntries(Handle handle, List<Entry> entries) {
        EntryDAO entryDAO = handle.attach(EntryDAO.class);
        entryDAO.insertInBatch(entries);
        entryDAO.setEntryNumber(entryDAO.currentEntryNumber() + entries.size());
    }

    public Optional<Entry> getEntry(int entryNumber) {
        return backingStoreDriver.getEntry(entryNumber);
    }

    public Collection<Entry> getEntries(int start, int limit) {
        return backingStoreDriver.getEntries(start, limit);
    }

    public Collection<Entry> getAllEntries() {
        return backingStoreDriver.getAllEntries();
    }

    public int getTotalEntries() {
        return backingStoreDriver.getTotalEntries();
    }

    public Optional<Instant> getLastUpdatedTime() {
        return backingStoreDriver.getLastUpdatedTime();
    }

    public RegisterProof getRegisterProof() throws NoSuchAlgorithmException {
        return backingStoreDriver.getRegisterProof();
    }

    public EntryProof getEntryProof(int entryNumber, int totalEntries) {
        return backingStoreDriver.getEntryProof(entryNumber, totalEntries);
    }

    public ConsistencyProof getConsistencyProof(int totalEntries1, int totalEntries2){
        return backingStoreDriver.getConsistencyProof(totalEntries1, totalEntries2);
    }
}
