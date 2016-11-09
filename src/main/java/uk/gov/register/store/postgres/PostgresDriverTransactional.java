package uk.gov.register.store.postgres;

import com.google.common.base.Function;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.exceptions.CallbackFailedException;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.tweak.HandleConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.core.*;
import uk.gov.register.db.*;
import uk.gov.register.service.RegisterService;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PostgresDriverTransactional extends PostgresDriver {

    private final static Logger LOG = LoggerFactory.getLogger(PostgresDriverTransactional.class);

    private final Handle handle;

    private final List<Entry> stagedEntries;
    private final Set<Item> stagedItems;
    private final HashMap<String, Integer> stagedCurrentKeys;

    private PostgresDriverTransactional(Handle handle, TransactionalMemoizationStore memoizationStore) {
        super(memoizationStore);

        this.handle = handle;
        this.stagedEntries = new ArrayList<>();
        this.stagedItems = new LinkedHashSet<>();
        this.stagedCurrentKeys = new HashMap<>();
    }

    protected PostgresDriverTransactional(Handle handle, MemoizationStore memoizationStore,
                                          Function<Handle, EntryQueryDAO> entryQueryDAO, Function<Handle, EntryDAO> entryDAO,
                                          Function<Handle, ItemQueryDAO> itemQueryDAO, Function<Handle, ItemDAO> itemDAO,
                                          Function<Handle, RecordQueryDAO> recordQueryDAO, Function<Handle, CurrentKeysUpdateDAO> currentKeysUpdateDAO) {
        super(entryQueryDAO, entryDAO, itemQueryDAO, itemDAO,  recordQueryDAO, currentKeysUpdateDAO, memoizationStore);

        this.handle = handle;
        this.stagedEntries = new ArrayList<>();
        this.stagedItems = new LinkedHashSet<>();
        this.stagedCurrentKeys = new HashMap<>();
    }

    public static void useTransaction(DBI dbi, MemoizationStore memoizationStore, Consumer<PostgresDriverTransactional> callback) {
        TransactionalMemoizationStore transactionalMemoizationStore = new TransactionalMemoizationStore(memoizationStore);
        Handle handle = dbi.open();

        try {
            handle.setTransactionIsolation(TransactionIsolationLevel.SERIALIZABLE);
            handle.begin();

            PostgresDriverTransactional postgresDriver = new PostgresDriverTransactional(handle, transactionalMemoizationStore);
            callback.accept(postgresDriver);
            postgresDriver.writeStagedData();

            transactionalMemoizationStore.commitHashesToStore();
            handle.commit();
        } catch (Exception ex) {
            LOG.error("",ex);
            handle.rollback();
            transactionalMemoizationStore.rollbackHashesFromStore();
            throw ex;
        } finally {
            handle.close();
        }
    }

    @Override
    public void insertEntry(Entry entry) {
        stagedEntries.add(entry);
    }

    @Override
    public void insertItem(Item item) {
        stagedItems.add(item);
    }

    @Override
    public void insertRecord(Record record, String registerName) {
        stagedCurrentKeys.put(record.item.getKey(registerName), record.entry.getEntryNumber());
    }

    @Override
    public Optional<Item> getItemBySha256(String sha256hex) {
        Optional<Item> stagedItem = searchStagingForItem(sha256hex);
        return stagedItem.isPresent() ? stagedItem : super.getItemBySha256(sha256hex);
    }

    @Override
    protected void useHandle(HandleConsumer callback) {
        useExistingHandle(callback);
    }

    @Override
    protected <ReturnType> ReturnType withHandle(HandleCallback<ReturnType> callback) {
        return writeDataUsingExistingHandle(callback);
    }

    @Override
    protected <ReturnType> ReturnType inTransaction(HandleCallback<ReturnType> callback) {
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
        writeEntries();
        writeItems();
        writeCurrentKeys();
    }

    private void writeEntries() {
        if (stagedEntries.isEmpty()) {
            return;
        }
        super.insertEntries(stagedEntries);
        stagedEntries.clear();
    }

    private void writeItems() {
        if (stagedItems.isEmpty()) {
            return;
        }
        super.insertItems(stagedItems);
        stagedItems.clear();
    }

    private void writeCurrentKeys() {
        if (stagedCurrentKeys.isEmpty()) {
            return;
        }

        List<CurrentKey> currentKeysToWrite = stagedCurrentKeys.entrySet().stream()
                .map(ck -> new CurrentKey(ck.getKey(), ck.getValue()))
                .collect(Collectors.toList());

        super.insertCurrentKeys(currentKeysToWrite);
        stagedCurrentKeys.clear();
    }

    private Optional<Item> searchStagingForItem(String sha256hex) {
        return stagedItems.stream().filter(item -> item.getSha256hex().equals(sha256hex)).findFirst();
    }
}
