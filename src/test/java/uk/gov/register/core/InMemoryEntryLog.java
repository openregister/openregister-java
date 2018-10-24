package uk.gov.register.core;

import uk.gov.register.db.*;
import uk.gov.register.store.postgres.BatchedPostgresDataAccessLayer;
import uk.gov.register.store.postgres.PostgresDataAccessLayer;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;

public class InMemoryEntryLog extends EntryLogImpl {
    private final EntryDAO entryDAO;

    public InMemoryEntryLog(MemoizationStore memoizationStore, EntryQueryDAO entryQueryDAO, EntryDAO entryDAO) {
        super(new BatchedPostgresDataAccessLayer(new PostgresDataAccessLayer(mock(EntryDAO.class), entryQueryDAO, mock(ItemDAO.class),
                mock(ItemQueryDAO.class), mock(RecordQueryDAO.class), "schema")), memoizationStore);
        this.entryDAO = entryDAO;
    }

    @Override
    public void appendEntry(Entry entry) {
        entryDAO.insertInBatch(singletonList(entry), "zzz", "entry");
    }
}
