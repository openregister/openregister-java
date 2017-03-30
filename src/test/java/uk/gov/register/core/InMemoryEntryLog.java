package uk.gov.register.core;

import static java.util.Collections.singletonList;

import uk.gov.register.db.AbstractEntryLog;
import uk.gov.register.db.EntryDAO;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.db.IndexQueryDAO;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

public class InMemoryEntryLog extends AbstractEntryLog {
    private final EntryDAO entryDAO;

    public InMemoryEntryLog(MemoizationStore memoizationStore, EntryQueryDAO entryQueryDAO, EntryDAO entryDAO, IndexQueryDAO indexQueryDAO) {
        super(entryQueryDAO, memoizationStore, indexQueryDAO);
        this.entryDAO = entryDAO;
    }

    @Override
    public void appendEntry(Entry entry) {
        entryDAO.insertInBatch(singletonList(entry));
    }
}
