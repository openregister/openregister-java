package uk.gov.register.core;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;

public class TransactionalMemoizationStoreTest {
    @Test
    public void getShouldNotReturnHashWhenNoHashExistsForGivenStartIndexAndTreeSize() {
        byte[] hash = "22905e52ce72f3880bddd5564966b15b017a038d84de41c390b2aafe28fd8452".getBytes();
        MemoizationStore internalStore = mock(MemoizationStore.class);
        when(internalStore.get(0, 2)).thenReturn(hash);
        TransactionalMemoizationStore store = createTransactionMemoizationStore(internalStore);

        byte[] result = store.get(1, 1);

        assertThat(result, is(nullValue()));
    }

    @Test
    public void getShouldReturnHashWhenHashExistsInOriginalMemoizationStore() {
        byte[] hash = "22905e52ce72f3880bddd5564966b15b017a038d84de41c390b2aafe28fd8452".getBytes();
        MemoizationStore internalStore = mock(MemoizationStore.class);
        when(internalStore.get(0, 1)).thenReturn(hash);
        TransactionalMemoizationStore store = createTransactionMemoizationStore(internalStore);

        byte[] result = store.get(0, 1);

        verify(internalStore).get(0, 1);
        assertThat(Arrays.equals(result, hash), is(true));
    }

    @Test
    public void getShouldReturnHashWhenHashHasBeenAddedButNotCommitted() {
        byte[] hash = "22905e52ce72f3880bddd5564966b15b017a038d84de41c390b2aafe28fd8452".getBytes();
        MemoizationStore internalStore = mock(MemoizationStore.class);
        TransactionalMemoizationStore store = createTransactionMemoizationStore(internalStore);

        store.put(0, 1, hash);
        byte[] result = store.get(0, 1);

        verify(internalStore, never()).get(0, 1);
        assertThat(Arrays.equals(result, hash), is(true));
    }

    @Test
    public void putShouldNotAddHashWhenDoingSoWouldRewriteHistory() {
        byte[] originalHash = "22905e52ce72f3880bddd5564966b15b017a038d84de41c390b2aafe28fd8452".getBytes();
        byte[] newHash = "1c3c123932880750cf05a415622a7f7f532b36c6c2c151964f60265b1c3b2dd3".getBytes();
        MemoizationStore internalStore = mock(MemoizationStore.class);
        TransactionalMemoizationStore store = createTransactionMemoizationStore(internalStore);

        // Add hash to empty store and commit
        store.put(0, 1, originalHash);
        store.commitHashesToStore();

        // Attempt to override previous hash with new hash
        store.put(0, 1, newHash);

        verify(internalStore, times(1)).put(0, 1, originalHash);
        verify(internalStore, never()).put(0, 1, newHash);
    }

    @Test
    public void putShouldNotPersistHashDirectlyToMemoizationStore() {
        ArrayList<byte[]> addedHashes = new ArrayList<>();
        byte[] hash = "22905e52ce72f3880bddd5564966b15b017a038d84de41c390b2aafe28fd8452".getBytes();
        MemoizationStore internalStore = mock(MemoizationStore.class);
        when(internalStore.get(0, 1)).thenReturn(hash);
        setupAddHashToList(internalStore, addedHashes);

        TransactionalMemoizationStore store = createTransactionMemoizationStore(internalStore);

        store.put(0, 1, hash);

        assertThat(addedHashes, hasSize(0));
    }

    @Test
    public void commitHashesToStoreShouldCommitAllHashesToMemoizationStore() {
        ArrayList<byte[]> addedHashes = new ArrayList<>();
        byte[] hash = "22905e52ce72f3880bddd5564966b15b017a038d84de41c390b2aafe28fd8452".getBytes();
        MemoizationStore internalStore = mock(MemoizationStore.class);
        when(internalStore.get(0, 1)).thenReturn(hash);
        setupAddHashToList(internalStore, addedHashes);

        TransactionalMemoizationStore store = createTransactionMemoizationStore(internalStore);

        store.put(0, 1, hash);
        store.put(1, 1, hash);
        assertThat(addedHashes, hasSize(0));

        store.commitHashesToStore();
        assertThat(addedHashes, hasSize(2));
    }

    @Test
    public void commitHashesToStoreShouldCommitOnlyLatestHashForGivenStartIndexAndSizeConstraints() {
        byte[] hash = "22905e52ce72f3880bddd5564966b15b017a038d84de41c390b2aafe28fd8452".getBytes();
        byte[] newHash = "1c3c123932880750cf05a415622a7f7f532b36c6c2c151964f60265b1c3b2dd3".getBytes();
        MemoizationStore internalStore = mock(MemoizationStore.class);
        TransactionalMemoizationStore store = createTransactionMemoizationStore(internalStore);

        store.put(0, 1, hash);
        store.put(0, 1, newHash);
        store.commitHashesToStore();

        verify(internalStore, never()).put(0, 1, hash);
        verify(internalStore, times(1)).put(0, 1, newHash);
;    }

    @Test
    public void rollbackHashesFromStoreShouldRemoveAllStagedHashes() {
        ArrayList<byte[]> addedHashes = new ArrayList<>();
        byte[] hash = "22905e52ce72f3880bddd5564966b15b017a038d84de41c390b2aafe28fd8452".getBytes();
        MemoizationStore internalStore = mock(MemoizationStore.class);
        when(internalStore.get(0, 1)).thenReturn(hash);
        setupAddHashToList(internalStore, addedHashes);

        TransactionalMemoizationStore store = createTransactionMemoizationStore(internalStore);

        store.put(0, 1, hash);
        store.rollbackHashesFromStore();

        // Commit any and all entries to prove that temporary entries have been cleared
        store.commitHashesToStore();

        assertThat(addedHashes, hasSize(0));
    }

    private TransactionalMemoizationStore createTransactionMemoizationStore(MemoizationStore memoizationStore) {
        if (memoizationStore == null) {
            memoizationStore = mock(MemoizationStore.class);
        }

        return new TransactionalMemoizationStore(memoizationStore);
    }

    private void setupAddHashToList(MemoizationStore store, List<byte[]> list) {
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        doAnswer(invocation -> {
            list.add(captor.getValue());
            return null;
        }).when(store).put(anyInt(), anyInt(), captor.capture());
    }
}