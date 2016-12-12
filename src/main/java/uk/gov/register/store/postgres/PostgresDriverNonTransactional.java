package uk.gov.register.store.postgres;

import com.google.common.collect.Lists;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.tweak.HandleConsumer;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Record;
import uk.gov.register.db.*;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.Collections.singletonList;

public class PostgresDriverNonTransactional {
    protected final MemoizationStore memoizationStore;
    private final Function<Handle, RecordQueryDAO> recordQueryDAOFromHandle;
    private final Function<Handle, CurrentKeysUpdateDAO> currentKeysUpdateDAOFromHandle;
    private DBI dbi;

    @Inject
    public PostgresDriverNonTransactional(DBI dbi, MemoizationStore memoizationStore) {
        this(dbi, memoizationStore,
                h -> h.attach(RecordQueryDAO.class), h -> h.attach(CurrentKeysUpdateDAO.class));
    }

    protected PostgresDriverNonTransactional(DBI dbi, MemoizationStore memoizationStore,
                                             Function<Handle, RecordQueryDAO> recordQueryDAO, Function<Handle, CurrentKeysUpdateDAO> currentKeysUpdateDAO) {
        this.recordQueryDAOFromHandle = recordQueryDAO;
        this.currentKeysUpdateDAOFromHandle = currentKeysUpdateDAO;
        this.memoizationStore = memoizationStore;
        this.dbi = dbi;
    }

    public void insertRecord(String key, Integer entryNumber) {
        insertCurrentKeys(singletonList(new CurrentKey(key, entryNumber)));
    }

    public Optional<Record> getRecord(String key) {
        HandleCallback<Optional<Record>> callback = handle -> recordQueryDAOFromHandle.apply(handle).findByPrimaryKey(key);
        return dbi.withHandle(callback);
    }

    public int getTotalRecords() {
        HandleCallback<Integer> callback = handle -> recordQueryDAOFromHandle.apply(handle).getTotalRecords();
        return dbi.withHandle(callback);
    }

    public List<Record> getRecords(int limit, int offset) {
        HandleCallback<List<Record>> callback = handle -> recordQueryDAOFromHandle.apply(handle).getRecords(limit, offset);
        return dbi.withHandle(callback);
    }

    public List<Record> findMax100RecordsByKeyValue(String key, String value) {
        HandleCallback<List<Record>> callback = handle -> recordQueryDAOFromHandle.apply(handle).findMax100RecordsByKeyValue(key, value);
        return dbi.withHandle(callback);
    }

    public Collection<Entry> findAllEntriesOfRecordBy(String registerName, String key) {
        HandleCallback<Collection<Entry>> callback = handle -> recordQueryDAOFromHandle.apply(handle).findAllEntriesOfRecordBy(registerName, key);
        return dbi.withHandle(callback);
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
