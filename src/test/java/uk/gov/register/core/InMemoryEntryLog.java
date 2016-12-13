package uk.gov.register.core;

import uk.gov.register.db.AbstractEntryLog;
import uk.gov.register.db.EntryDAO;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import static java.util.Collections.singletonList;

public class InMemoryEntryLog extends AbstractEntryLog {
    private final EntryDAO entryDAO;

    public InMemoryEntryLog(MemoizationStore memoizationStore, EntryQueryDAO entryQueryDAO, EntryDAO entryDAO) {
        super(entryQueryDAO, memoizationStore);
        this.entryDAO = entryDAO;
    }

    @Override
    public void appendEntry(Entry entry) {
        entryDAO.insertInBatch(singletonList(entry));
    }
}
