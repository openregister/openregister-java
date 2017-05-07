package uk.gov.register.db;

import uk.gov.register.core.Entry;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

/**
 * An append-only log of Entries, together with proofs
 */
public class TransactionalEntryLog extends AbstractEntryLog {
    private final DataAccessLayer dataAccessLayer;

    public TransactionalEntryLog(MemoizationStore memoizationStore, DataAccessLayer dataAccessLayer) {
        super(dataAccessLayer, memoizationStore);
        this.dataAccessLayer = dataAccessLayer;
    }

    @Override
    public void appendEntry(Entry entry) {
        dataAccessLayer.appendEntry(entry);
    }
}
