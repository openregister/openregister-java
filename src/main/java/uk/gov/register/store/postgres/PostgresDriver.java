package uk.gov.register.store.postgres;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.codec.digest.DigestUtils;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.tweak.HandleConsumer;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.db.*;
import uk.gov.register.store.BackingStoreDriver;
import uk.gov.verifiablelog.VerifiableLog;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import java.time.Instant;
import java.util.*;

public abstract class PostgresDriver implements BackingStoreDriver {

    protected final MemoizationStore memoizationStore;
    private final Function<Handle, EntryQueryDAO> entryQueryDAOFromHandle;
    private final Function<Handle, EntryDAO> entryDAOFromHandle;
    private final Function<Handle, ItemQueryDAO> itemQueryDAOFromHandle;
    private final Function<Handle, ItemDAO> itemDAOFromHandle;
    private final Function<Handle, RecordQueryDAO> recordQueryDAOFromHandle;
    private final Function<Handle, CurrentKeysUpdateDAO> currentKeysUpdateDAOFromHandle;

    public PostgresDriver(MemoizationStore memoizationStore) {
        this(h -> h.attach(EntryQueryDAO.class), h -> h.attach(EntryDAO.class),
                h -> h.attach(ItemQueryDAO.class), h -> h.attach(ItemDAO.class),
                h -> h.attach(RecordQueryDAO.class), h -> h.attach(CurrentKeysUpdateDAO.class), memoizationStore);
    }

    protected PostgresDriver(
            Function<Handle, EntryQueryDAO> entryQueryDAOFromHandle,
            Function<Handle, EntryDAO> entryDAOFromHandle,
            Function<Handle, ItemQueryDAO> itemQueryDAOFromHandle,
            Function<Handle, ItemDAO> itemDAOFromHandle,
            Function<Handle, RecordQueryDAO> recordQueryDAOFromHandle,
            Function<Handle, CurrentKeysUpdateDAO> currentKeysUpdateDAOFromHandle,
            MemoizationStore memoizationStore) {
        this.entryQueryDAOFromHandle = entryQueryDAOFromHandle;
        this.entryDAOFromHandle = entryDAOFromHandle;
        this.itemQueryDAOFromHandle = itemQueryDAOFromHandle;
        this.itemDAOFromHandle = itemDAOFromHandle;
        this.recordQueryDAOFromHandle = recordQueryDAOFromHandle;
        this.currentKeysUpdateDAOFromHandle = currentKeysUpdateDAOFromHandle;
        this.memoizationStore = memoizationStore;
    }

    @Override
    public abstract void insertEntry(Entry entry);

    @Override
    public abstract void insertItem(Item item);

    @Override
    public abstract void insertRecord(Record record, String registerName);

    @Override
    public Optional<Entry> getEntry(int entryNumber) {
        return withHandle(handle -> entryQueryDAOFromHandle.apply(handle).findByEntryNumber(entryNumber));
    }

    @Override
    public Collection<Entry> getEntries(int start, int limit) {
        return withHandle(handle -> entryQueryDAOFromHandle.apply(handle).getEntries(start, limit));
    }

    @Override
    public Collection<Entry> getAllEntries() {
        return withHandle(handle -> entryQueryDAOFromHandle.apply(handle).getAllEntriesNoPagination());
    }

    @Override
    public int getTotalEntries() {
        return withHandle(handle -> entryQueryDAOFromHandle.apply(handle).getTotalEntries());
    }

    @Override
    public Optional<Instant> getLastUpdatedTime() {
        return withHandle(handle -> entryQueryDAOFromHandle.apply(handle).getLastUpdatedTime());
    }

    @Override
    public Optional<Item> getItemBySha256(String sha256hex) {
        return withHandle(handle -> itemQueryDAOFromHandle.apply(handle).getItemBySHA256(sha256hex));
    }

    @Override
    public Collection<Item> getAllItems() {
        return withHandle(handle -> itemQueryDAOFromHandle.apply(handle).getAllItemsNoPagination());
    }

    @Override
    public Optional<Record> getRecord(String key) {
        return withHandle(handle -> recordQueryDAOFromHandle.apply(handle).findByPrimaryKey(key));
    }

    @Override
    public int getTotalRecords() {
        return withHandle(handle -> recordQueryDAOFromHandle.apply(handle).getTotalRecords());
    }

    @Override
    public List<Record> getRecords(int limit, int offset) {
        return withHandle(handle -> recordQueryDAOFromHandle.apply(handle).getRecords(limit, offset));
    }

    @Override
    public List<Record> findMax100RecordsByKeyValue(String key, String value) {
        return withHandle(handle -> recordQueryDAOFromHandle.apply(handle).findMax100RecordsByKeyValue(key, value));
    }

    @Override
    public Collection<Entry> findAllEntriesOfRecordBy(String registerName, String key) {
        return withHandle(handle -> recordQueryDAOFromHandle.apply(handle).findAllEntriesOfRecordBy(registerName, key));
    }

    @Override
    public <ReturnType> ReturnType withVerifiableLog(Function<VerifiableLog, ReturnType> callback) {
        return inTransaction(handle -> callback.apply(getVerifiableLog(handle)));
    }

    protected void insertEntries(Iterable<Entry> entries) {
        useHandle(handle -> {
            EntryDAO dao = entryDAOFromHandle.apply(handle);
            dao.insertInBatch(entries);
            dao.setEntryNumber(dao.currentEntryNumber() + Iterables.size(entries));
        });
    }

    protected void insertItems(Iterable<Item> items) {
        useHandle(handle -> itemDAOFromHandle.apply(handle).insertInBatch(items));
    }

    protected void insertCurrentKeys(List<CurrentKey> currentKeys) {
        useHandle(handle -> {
            CurrentKeysUpdateDAO dao = currentKeysUpdateDAOFromHandle.apply(handle);

            int[] noOfRecordsDeletedPerBatch = dao.removeRecordWithKeys(Lists.transform(currentKeys, r -> r.getKey()));
            int noOfRecordsDeleted = IntStream.of(noOfRecordsDeletedPerBatch).sum();
            dao.writeCurrentKeys(currentKeys);
            dao.updateTotalRecords(currentKeys.size() - noOfRecordsDeleted);
        });
    }

    private VerifiableLog getVerifiableLog(Handle handle) {
        EntryMerkleLeafStore merkleLeafStore = new EntryMerkleLeafStore(entryQueryDAOFromHandle.apply(handle));
        return new VerifiableLog(DigestUtils.getSha256Digest(), merkleLeafStore, memoizationStore);
    }

    protected abstract void useHandle(HandleConsumer callback);

    protected abstract <ReturnType> ReturnType withHandle(HandleCallback<ReturnType> callback);

    protected abstract <ReturnType> ReturnType inTransaction(HandleCallback<ReturnType> callback);
}
