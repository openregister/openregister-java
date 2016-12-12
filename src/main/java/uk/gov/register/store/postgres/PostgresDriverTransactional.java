package uk.gov.register.store.postgres;

import com.google.common.collect.Lists;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.exceptions.CallbackFailedException;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.tweak.HandleConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Record;
import uk.gov.register.db.*;
import uk.gov.register.store.BackingStoreDriver;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PostgresDriverTransactional implements BackingStoreDriver {

    private final static Logger LOG = LoggerFactory.getLogger(PostgresDriverTransactional.class);

    private final Handle handle;

    private final HashMap<String, Integer> stagedCurrentKeys;
    private final Function<Handle, RecordQueryDAO> recordQueryDAOFromHandle;
    private final Function<Handle, CurrentKeysUpdateDAO> currentKeysUpdateDAOFromHandle;

    private PostgresDriverTransactional(Handle handle) {
        this(handle,
                h -> h.attach(RecordQueryDAO.class), h -> h.attach(CurrentKeysUpdateDAO.class));
    }

    protected PostgresDriverTransactional(Handle handle,
                                          Function<Handle, RecordQueryDAO> recordQueryDAO, Function<Handle, CurrentKeysUpdateDAO> currentKeysUpdateDAO) {
        this.recordQueryDAOFromHandle = recordQueryDAO;
        this.currentKeysUpdateDAOFromHandle = currentKeysUpdateDAO;

        this.handle = handle;
        this.stagedCurrentKeys = new HashMap<>();
    }

    public static void useTransaction(DBI dbi, Consumer<PostgresDriverTransactional> callback) {
        Handle handle = dbi.open();

        try {
            handle.setTransactionIsolation(TransactionIsolationLevel.SERIALIZABLE);
            handle.begin();

            PostgresDriverTransactional postgresDriver = new PostgresDriverTransactional(handle);
            callback.accept(postgresDriver);
            postgresDriver.writeStagedData();

            handle.commit();
        } catch (Exception ex) {
            LOG.error("", ex);
            handle.rollback();
            throw ex;
        } finally {
            handle.close();
        }
    }

    @Override
    public void insertRecord(String key, Integer entryNumber) {
        stagedCurrentKeys.put(key, entryNumber);
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
        writeCurrentKeys();
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

    public Handle getHandle() {
        return handle;
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
