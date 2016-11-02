package uk.gov.register.core;

import uk.gov.verifiablelog.store.memoization.MemoizationStore;
import java.util.HashMap;

public class TransactionalMemoizationStore implements MemoizationStore {
    private final MemoizationStore memoizationStore;
    private final HashMap<Integer, HashMap<Integer, byte[]>> stagedHashes;
    private Integer currentEntryCount;

    public TransactionalMemoizationStore(MemoizationStore memoizationStore) {
        this.memoizationStore = memoizationStore;
        this.stagedHashes = new HashMap<>();
        this.currentEntryCount = 0;
    }

    @Override
    public void put(Integer start, Integer size, byte[] value) {
        if (!stagedHashes.containsKey(start)) {
            stagedHashes.put(start, new HashMap<>());
        }

        HashMap<Integer, byte[]> hashesBySize = stagedHashes.get(start);
        hashesBySize.put(size, value);
    }

    @Override
    public byte[] get(Integer start, Integer size) {
        byte[] hash = null;

        if (stagedHashes.containsKey(start)) {
            hash = stagedHashes.get(start).get(size);
        }

        return hash == null ? memoizationStore.get(start, size) : hash;
    }

    public void setCurrentEntryCount(int currentEntryCount) {
        this.currentEntryCount = currentEntryCount;
    }

    public void commitHashesToStore() {
        stagedHashes.forEach((start, hashesBySize) -> {
            hashesBySize.forEach((size, hash) -> {
                memoizationStore.put(start, size, hash);
            });
        });

        setCurrentEntryCount(stagedHashes.size() + currentEntryCount);

        stagedHashes.clear();
    }

    public void rollbackHashesFromStore() {
        stagedHashes.clear();
    }
}