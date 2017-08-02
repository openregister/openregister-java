package uk.gov.register.core;

import uk.gov.register.db.*;
import uk.gov.register.store.postgres.PostgresDataAccessLayer;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;

public class InMemoryEntryLog extends EntryLogImpl {
    private final EntryDAO entryDAO;

    public InMemoryEntryLog(MemoizationStore memoizationStore, EntryQueryDAO entryQueryDAO, EntryDAO entryDAO) {
        super(new PostgresDataAccessLayer(entryQueryDAO, mock(IndexDAO.class), mock(IndexQueryDAO.class), mock(EntryDAO.class),
                mock(EntryItemDAO.class), mock(ItemQueryDAO.class), mock(ItemDAO.class), "schema"), memoizationStore);
        this.entryDAO = entryDAO;
    }

    @Override
    public void appendEntry(Entry entry) {
        entryDAO.insertInBatch(singletonList(entry), "zzz", "entry");
    }
}
