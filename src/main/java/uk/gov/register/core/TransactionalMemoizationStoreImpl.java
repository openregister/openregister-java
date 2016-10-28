package uk.gov.register.core;

import uk.gov.verifiablelog.store.memoization.MemoizationStore;
import java.util.HashMap;

public class TransactionalMemoizationStoreImpl implements TransactionalMemoizationStore {
    private final MemoizationStore instance;
    private final HashMap<Integer, HashMap<Integer, byte[]>> temporaryEntries;
    private Integer currentEntryCount;

    public TransactionalMemoizationStoreImpl(
            MemoizationStore instance) {
        this.instance = instance;
        this.temporaryEntries = new HashMap<>();
    }

    @Override
    public void setCurrentEntryCount(int currentEntryCount) {
        this.currentEntryCount = currentEntryCount;
    }

    @Override
    public void put(Integer start, Integer size, byte[] value) {
        if ((start + size) <= currentEntryCount) {
            return;
        }

        if (!temporaryEntries.containsKey(start)) {
            temporaryEntries.put(start, new HashMap<>());
        }

        HashMap<Integer, byte[]> hashesBySize = temporaryEntries.get(start);
        hashesBySize.put(size, value);
    }

    @Override
    public byte[] get(Integer start, Integer size) {
        boolean hashIsTemporary = (start + size) > currentEntryCount;

        if (hashIsTemporary) {
            if (!temporaryEntries.containsKey(start)) {
                return null;
            }

            HashMap<Integer, byte[]> hashesBySize = temporaryEntries.get(start);
            return hashesBySize.get(size);
        }

        return instance.get(start, size);
    }

    @Override
    public void commitEntries() {
        temporaryEntries.forEach((start, hashesBySize) -> {
            hashesBySize.forEach((size, hash) -> {
                instance.put(start, size, hash);
            });
        });
        temporaryEntries.clear();

        setCurrentEntryCount(temporaryEntries.size() + currentEntryCount);
    }

    @Override
    public void rollbackEntries() {
        temporaryEntries.clear();
    }
}