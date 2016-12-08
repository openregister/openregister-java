package uk.gov.register.store.postgres;

import com.google.common.collect.Lists;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.ResultIterator;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.exceptions.CallbackFailedException;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.tweak.HandleConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.core.TransactionalMemoizationStore;
import uk.gov.register.db.*;
import uk.gov.register.store.BackingStoreDriver;
import uk.gov.register.util.HashValue;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PostgresDriverTransactional implements BackingStoreDriver {

    private final static Logger LOG = LoggerFactory.getLogger(PostgresDriverTransactional.class);

    private final Handle handle;

    private final HashMap<HashValue, Item> stagedItems;
    private final HashMap<String, Integer> stagedCurrentKeys;
    private final Function<Handle, ItemQueryDAO> itemQueryDAOFromHandle;
    private final Function<Handle, ItemDAO> itemDAOFromHandle;
    private final Function<Handle, RecordQueryDAO> recordQueryDAOFromHandle;
    private final Function<Handle, CurrentKeysUpdateDAO> currentKeysUpdateDAOFromHandle;

    private PostgresDriverTransactional(Handle handle) {
        this(handle,
                h -> h.attach(ItemQueryDAO.class), h -> h.attach(ItemDAO.class),
                h -> h.attach(RecordQueryDAO.class), h -> h.attach(CurrentKeysUpdateDAO.class));
    }

    protected PostgresDriverTransactional(Handle handle,
                                          Function<Handle, ItemQueryDAO> itemQueryDAO, Function<Handle, ItemDAO> itemDAO,
                                          Function<Handle, RecordQueryDAO> recordQueryDAO, Function<Handle, CurrentKeysUpdateDAO> currentKeysUpdateDAO) {
        this.itemQueryDAOFromHandle = itemQueryDAO;
        this.itemDAOFromHandle = itemDAO;
        this.recordQueryDAOFromHandle = recordQueryDAO;
        this.currentKeysUpdateDAOFromHandle = currentKeysUpdateDAO;

        this.handle = handle;
        this.stagedItems = new HashMap<>();
        this.stagedCurrentKeys = new HashMap<>();
    }

    public static void useTransaction(DBI dbi, MemoizationStore memoizationStore, Consumer<PostgresDriverTransactional> callback) {
        TransactionalMemoizationStore transactionalMemoizationStore = new TransactionalMemoizationStore(memoizationStore);
        Handle handle = dbi.open();

        try {
            handle.setTransactionIsolation(TransactionIsolationLevel.SERIALIZABLE);
            handle.begin();

            PostgresDriverTransactional postgresDriver = new PostgresDriverTransactional(handle);
            callback.accept(postgresDriver);
            postgresDriver.writeStagedData();

            transactionalMemoizationStore.commitHashesToStore();
            handle.commit();
        } catch (Exception ex) {
            LOG.error("", ex);
            handle.rollback();
            transactionalMemoizationStore.rollbackHashesFromStore();
            throw ex;
        } finally {
            handle.close();
        }
    }

    @Override
    public void insertItem(Item item) {
        stagedItems.put(item.getSha256hex(), item);
    }

    @Override
    public void insertRecord(Record record, String registerName) {
        stagedCurrentKeys.put(record.item.getValue(registerName), record.entry.getEntryNumber());
    }

    @Override
    public Optional<Item> getItemBySha256(HashValue hash) {
        Optional<Item> stagedItem = searchStagingForItem(hash);
        return stagedItem.isPresent() ? stagedItem : getItemFromDao(hash);
    }

    public Optional<Item> getItemFromDao(HashValue hash) {
        HandleCallback<Optional<Item>> callback = handle -> itemQueryDAOFromHandle.apply(handle).getItemBySHA256(hash.getValue());
        return writeDataUsingExistingHandle(callback);
    }

    private void useExistingHandle(HandleConsumer callback) {
        try {
            callback.useHandle(handle);
        } catch (Exception ex) {
            throw new CallbackFailedException(ex);
        }
    }

    private <ReturnType> ReturnType writeDataUsingExistingHandle(HandleCallback<ReturnType> callback) {
        try {
            writeStagedData();
            return callback.withHandle(handle);
        } catch (Exception ex) {
            throw new CallbackFailedException(ex);
        }
    }

    private void writeStagedData() {
        writeItems();
        writeCurrentKeys();
    }

    private void writeItems() {
        if (stagedItems.isEmpty()) {
            return;
        }
        insertItems(stagedItems.values());
        stagedItems.clear();
    }

    private void writeCurrentKeys() {
        if (stagedCurrentKeys.isEmpty()) {
            return;
        }

        List<CurrentKey> currentKeysToWrite = stagedCurrentKeys.entrySet().stream()
                .map(ck -> new CurrentKey(ck.getKey(), ck.getValue()))
                .collect(Collectors.toList());

        insertCurrentKeys(currentKeysToWrite);
        stagedCurrentKeys.clear();
    }

    private Optional<Item> searchStagingForItem(HashValue hash) {
        return Optional.ofNullable(stagedItems.get(hash));
    }

    public Handle getHandle() {
        return handle;
    }

    @Override
    public Iterator<Item> getItemIterator() {
        HandleCallback<ResultIterator<Item>> callback = handle -> itemQueryDAOFromHandle.apply(handle).getIterator();
        return writeDataUsingExistingHandle(callback);
    }

    @Override
    public Iterator<Item> getItemIterator(int start, int end) {
        HandleCallback<ResultIterator<Item>> callback = handle -> itemQueryDAOFromHandle.apply(handle).getIterator(start, end);
        return writeDataUsingExistingHandle(callback);
    }

    @Override
    public Collection<Item> getAllItems() {
        HandleCallback<Collection<Item>> callback = handle -> itemQueryDAOFromHandle.apply(handle).getAllItemsNoPagination();
        return writeDataUsingExistingHandle(callback);
    }

    @Override
    public Optional<Record> getRecord(String key) {
        HandleCallback<Optional<Record>> callback = handle -> recordQueryDAOFromHandle.apply(handle).findByPrimaryKey(key);
        return writeDataUsingExistingHandle(callback);
    }

    @Override
    public int getTotalRecords() {
        HandleCallback<Integer> callback = handle -> recordQueryDAOFromHandle.apply(handle).getTotalRecords();
        return writeDataUsingExistingHandle(callback);
    }

    @Override
    public List<Record> getRecords(int limit, int offset) {
        HandleCallback<List<Record>> callback = handle -> recordQueryDAOFromHandle.apply(handle).getRecords(limit, offset);
        return writeDataUsingExistingHandle(callback);
    }

    @Override
    public List<Record> findMax100RecordsByKeyValue(String key, String value) {
        HandleCallback<List<Record>> callback = handle -> recordQueryDAOFromHandle.apply(handle).findMax100RecordsByKeyValue(key, value);
        return writeDataUsingExistingHandle(callback);
    }

    @Override
    public Collection<Entry> findAllEntriesOfRecordBy(String registerName, String key) {
        HandleCallback<Collection<Entry>> callback = handle -> recordQueryDAOFromHandle.apply(handle).findAllEntriesOfRecordBy(registerName, key);
        return writeDataUsingExistingHandle(callback);
    }

    private void insertItems(Iterable<Item> items) {
        HandleConsumer callback = handle -> itemDAOFromHandle.apply(handle).insertInBatch(items);
        useExistingHandle(callback);
    }

    private void insertCurrentKeys(List<CurrentKey> currentKeys) {
        HandleConsumer callback = handle -> {
            CurrentKeysUpdateDAO dao = currentKeysUpdateDAOFromHandle.apply(handle);

            int[] noOfRecordsDeletedPerBatch = dao.removeRecordWithKeys(Lists.transform(currentKeys, CurrentKey::getKey));
            int noOfRecordsDeleted = IntStream.of(noOfRecordsDeletedPerBatch).sum();
            dao.writeCurrentKeys(currentKeys);
            dao.updateTotalRecords(currentKeys.size() - noOfRecordsDeleted);
        };
        useExistingHandle(callback);
    }

}
