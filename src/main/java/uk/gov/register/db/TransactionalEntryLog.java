package uk.gov.register.db;

import uk.gov.register.core.Entry;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

/**
 * An append-only log of Entries, together with proofs
 */
public class TransactionalEntryLog extends AbstractEntryLog {
    private final List<Entry> stagedEntries;
    private final EntryQueryDAO entryQueryDAO;
    private final EntryDAO entryDAO;

    public TransactionalEntryLog(MemoizationStore memoizationStore, EntryQueryDAO entryQueryDAO, EntryDAO entryDAO) {
        super(entryQueryDAO, memoizationStore);
        this.stagedEntries = new ArrayList<>();
        this.entryQueryDAO = entryQueryDAO;
        this.entryDAO = entryDAO;
    }

    @Override
    public void appendEntry(Entry entry) {
        stagedEntries.add(entry);
    }

    @Override
    public int getTotalEntries() {
        // This method is called a lot, so we want to avoid checkpointing
        // every time it's called.  Instead we compute the result from stagedEntries,
        // falling back to the DB if necessary.
        OptionalInt maxStagedEntryNumber = getMaxStagedEntryNumber();
        return maxStagedEntryNumber.orElseGet(entryQueryDAO::getTotalEntries);
    }

    private OptionalInt getMaxStagedEntryNumber() {
        if (stagedEntries.isEmpty()) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(stagedEntries.get(stagedEntries.size() - 1).getEntryNumber());
    }

    @Override
    public void checkpoint() {
        if (stagedEntries.isEmpty()) {
            return;
        }
        entryDAO.insertInBatch(stagedEntries);
        entryDAO.setEntryNumber(entryDAO.currentEntryNumber() + stagedEntries.size());
        stagedEntries.clear();
    }
}
