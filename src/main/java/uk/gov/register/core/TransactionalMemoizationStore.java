package uk.gov.register.core;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;
import java.util.HashMap;

public class TransactionalMemoizationStore implements MemoizationStore {
    private final MemoizationStore memoizationStore;
    private final HashMap<Pair<Integer,Integer>, byte[]> stagedHashes;

    public TransactionalMemoizationStore(MemoizationStore memoizationStore) {
        this.memoizationStore = memoizationStore;
        this.stagedHashes = new HashMap<>();
    }

    @Override
    public void put(Integer start, Integer size, byte[] value) {
        stagedHashes.put(Pair.of(start, size), value);
    }

    @Override
    public byte[] get(Integer start, Integer size) {
        Pair<Integer, Integer> startSizePair = Pair.of(start, size);
        if (stagedHashes.containsKey(startSizePair))  {
            return stagedHashes.get(startSizePair);
        }

        return memoizationStore.get(start, size);
    }

    public void commitHashesToStore() {
        stagedHashes.forEach((startSizePair, hash) -> {
            memoizationStore.put(startSizePair.getLeft(), startSizePair.getRight(), hash);
        });

        stagedHashes.clear();
    }

    public void rollbackHashesFromStore() {
        stagedHashes.clear();
    }
}