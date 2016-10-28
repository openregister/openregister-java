package uk.gov.register.core;

import uk.gov.verifiablelog.store.memoization.MemoizationStore;

public interface TransactionalMemoizationStore extends MemoizationStore {
    void commitEntries();
    void rollbackEntries();
    void setCurrentEntryCount(int entryCutoff);
}