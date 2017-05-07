package uk.gov.register.db;

import uk.gov.register.core.Entry;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;


/**
 * An append-only log of Entries, together with proofs
 */
public class UnmodifiableEntryLog extends AbstractEntryLog {
    public UnmodifiableEntryLog(MemoizationStore memoizationStore, DataAccessLayer dataAccessLayer) {
        super(dataAccessLayer, memoizationStore);
    }

    @Override
    public void appendEntry(Entry entry) {
        throw new UnsupportedOperationException();
    }
}
