package uk.gov.register.core;

import uk.gov.register.db.EntryDAO;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.db.OnDemandEntryLog;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import static java.util.Collections.singletonList;

public class InMemoryEntryLog extends OnDemandEntryLog {
    private final EntryDAO entryDAO;

    public InMemoryEntryLog(MemoizationStore memoizationStore, EntryQueryDAO entryQueryDAO, EntryDAO entryDAO) {
        super(memoizationStore, entryQueryDAO);
        this.entryDAO = entryDAO;
    }

    @Override
    public void appendEntry(Entry entry) {
        entryDAO.insertInBatch(singletonList(entry));
    }

}
