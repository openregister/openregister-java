package uk.gov.register.store.postgres;

import com.google.common.collect.Lists;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.tweak.HandleConsumer;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.db.*;
import uk.gov.register.store.BackingStoreDriver;
import uk.gov.register.util.HashValue;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.Collections.singletonList;

public class PostgresDriverNonTransactional implements BackingStoreDriver {
    protected final MemoizationStore memoizationStore;
    private final Function<Handle, ItemQueryDAO> itemQueryDAOFromHandle;
    private final Function<Handle, ItemDAO> itemDAOFromHandle;
    private final Function<Handle, RecordQueryDAO> recordQueryDAOFromHandle;
    private final Function<Handle, CurrentKeysUpdateDAO> currentKeysUpdateDAOFromHandle;
    private DBI dbi;

    @Inject
    public PostgresDriverNonTransactional(DBI dbi, MemoizationStore memoizationStore) {
        this(dbi, memoizationStore,
                h -> h.attach(ItemQueryDAO.class), h -> h.attach(ItemDAO.class),
                h -> h.attach(RecordQueryDAO.class), h -> h.attach(CurrentKeysUpdateDAO.class));
    }

    protected PostgresDriverNonTransactional(DBI dbi, MemoizationStore memoizationStore,
                                             Function<Handle, ItemQueryDAO> itemQueryDAO, Function<Handle, ItemDAO> itemDAO,
                                             Function<Handle, RecordQueryDAO> recordQueryDAO, Function<Handle, CurrentKeysUpdateDAO> currentKeysUpdateDAO) {
        this.itemQueryDAOFromHandle = itemQueryDAO;
        this.itemDAOFromHandle = itemDAO;
        this.recordQueryDAOFromHandle = recordQueryDAO;
        this.currentKeysUpdateDAOFromHandle = currentKeysUpdateDAO;
        this.memoizationStore = memoizationStore;
        this.dbi = dbi;
    }

    @Override
    public void insertItem(Item item) {
        insertItems(singletonList(item));
    }

    @Override
    public void insertRecord(Record record, String registerName) {
        insertCurrentKeys(singletonList(new CurrentKey(record.item.getValue(registerName), record.entry.getEntryNumber())));
    }

    @Override
    public Iterator<Item> getItemIterator(){
        HandleCallback<org.skife.jdbi.v2.ResultIterator<Item>> callback = handle -> itemQueryDAOFromHandle.apply(handle).getIterator();
        return dbi.withHandle(callback);
    }

    @Override
    public Iterator<Item> getItemIterator(int start, int end){
        HandleCallback<org.skife.jdbi.v2.ResultIterator<Item>> callback = handle -> itemQueryDAOFromHandle.apply(handle).getIterator(start, end);
        return dbi.withHandle(callback);
    }

    @Override
    public Optional<Item> getItemBySha256(HashValue hash) {
        HandleCallback<Optional<Item>> callback = handle -> itemQueryDAOFromHandle.apply(handle).getItemBySHA256(hash.getValue());
        return dbi.withHandle(callback);
    }

    @Override
    public Collection<Item> getAllItems() {
        HandleCallback<Collection<Item>> callback = handle -> itemQueryDAOFromHandle.apply(handle).getAllItemsNoPagination();
        return dbi.withHandle(callback);
    }

    @Override
    public Optional<Record> getRecord(String key) {
        HandleCallback<Optional<Record>> callback = handle -> recordQueryDAOFromHandle.apply(handle).findByPrimaryKey(key);
        return dbi.withHandle(callback);
    }

    @Override
    public int getTotalRecords() {
        HandleCallback<Integer> callback = handle -> recordQueryDAOFromHandle.apply(handle).getTotalRecords();
        return dbi.withHandle(callback);
    }

    @Override
    public List<Record> getRecords(int limit, int offset) {
        HandleCallback<List<Record>> callback = handle -> recordQueryDAOFromHandle.apply(handle).getRecords(limit, offset);
        return dbi.withHandle(callback);
    }

    @Override
    public List<Record> findMax100RecordsByKeyValue(String key, String value) {
        HandleCallback<List<Record>> callback = handle -> recordQueryDAOFromHandle.apply(handle).findMax100RecordsByKeyValue(key, value);
        return dbi.withHandle(callback);
    }

    @Override
    public Collection<Entry> findAllEntriesOfRecordBy(String registerName, String key) {
        HandleCallback<Collection<Entry>> callback = handle -> recordQueryDAOFromHandle.apply(handle).findAllEntriesOfRecordBy(registerName, key);
        return dbi.withHandle(callback);
    }

    protected void insertItems(Iterable<Item> items) {
        HandleConsumer callback = handle -> itemDAOFromHandle.apply(handle).insertInBatch(items);
        dbi.useHandle(callback);
    }

    protected void insertCurrentKeys(List<CurrentKey> currentKeys) {
        HandleConsumer callback = handle -> {
            CurrentKeysUpdateDAO dao = currentKeysUpdateDAOFromHandle.apply(handle);

            int[] noOfRecordsDeletedPerBatch = dao.removeRecordWithKeys(Lists.transform(currentKeys, CurrentKey::getKey));
            int noOfRecordsDeleted = IntStream.of(noOfRecordsDeletedPerBatch).sum();
            dao.writeCurrentKeys(currentKeys);
            dao.updateTotalRecords(currentKeys.size() - noOfRecordsDeleted);
        };
        dbi.useHandle(callback);
    }

}
