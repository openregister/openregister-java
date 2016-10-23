package uk.gov.register.store.postgres;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.codec.digest.DigestUtils;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionCallback;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.tweak.HandleConsumer;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.db.*;
import uk.gov.register.store.BackingStoreDriver;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;
import uk.gov.verifiablelog.VerifiableLog;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class PostgresDriver implements BackingStoreDriver {

    protected final MemoizationStore memoizationStore;

    public PostgresDriver(MemoizationStore memoizationStore) {
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
        return withHandle(handle -> handle.attach(EntryQueryDAO.class).findByEntryNumber(entryNumber));
    }

    @Override
    public Collection<Entry> getEntries(int start, int limit) {
        return withHandle(handle -> handle.attach(EntryQueryDAO.class).getEntries(start, limit));
    }

    @Override
    public Collection<Entry> getAllEntries() {
        return withHandle(handle -> handle.attach(EntryQueryDAO.class).getAllEntriesNoPagination());
    }

    @Override
    public int getTotalEntries() {
        return withHandle(handle -> handle.attach(EntryQueryDAO.class).getTotalEntries());
    }

    @Override
    public Optional<Instant> getLastUpdatedTime() {
        return withHandle(handle -> handle.attach(EntryQueryDAO.class).getLastUpdatedTime());
    }

    @Override
    public Optional<Item> getItemBySha256(String sha256hex) {
        return withHandle(handle -> handle.attach(ItemQueryDAO.class).getItemBySHA256(sha256hex));
    }

    @Override
    public Collection<Item> getAllItems() {
        return withHandle(handle -> handle.attach(ItemQueryDAO.class).getAllItemsNoPagination());
    }

    @Override
    public Optional<Record> getRecord(String key) {
        return withHandle(handle -> handle.attach(RecordQueryDAO.class).findByPrimaryKey(key));
    }

    @Override
    public int getTotalRecords() {
        return withHandle(handle -> handle.attach(RecordQueryDAO.class).getTotalRecords());
    }

    @Override
    public List<Record> getRecords(int limit, int offset) {
        return withHandle(handle -> handle.attach(RecordQueryDAO.class).getRecords(limit, offset));
    }

    @Override
    public List<Record> findMax100RecordsByKeyValue(String key, String value) {
        return withHandle(handle -> handle.attach(RecordQueryDAO.class).findMax100RecordsByKeyValue(key, value));
    }

    @Override
    public Collection<Entry> findAllEntriesOfRecordBy(String registerName, String key) {
        return withHandle(handle -> handle.attach(RecordQueryDAO.class).findAllEntriesOfRecordBy(registerName, key));
    }

    @Override
    public RegisterProof getRegisterProof() throws NoSuchAlgorithmException {
        return inTransaction((handle, status) -> {
            VerifiableLog verifiableLog = getVerifiableLog(handle);
            String rootHash = bytesToString(verifiableLog.currentRoot());
            return new RegisterProof(rootHash);
        });
    }

    @Override
    public EntryProof getEntryProof(int entryNumber, int totalEntries) {
        return inTransaction((handle, status) -> {
            VerifiableLog verifiableLog = getVerifiableLog(handle);

            List<String> auditProof = verifiableLog.auditProof(entryNumber, totalEntries).stream()
                    .map(this::bytesToString).collect(Collectors.toList());

            return new EntryProof(Integer.toString(entryNumber), auditProof);
        });
    }

    @Override
    public ConsistencyProof getConsistencyProof(int totalEntries1, int totalEntries2) {
        return inTransaction((handle, status) -> {
            VerifiableLog verifiableLog = getVerifiableLog(handle);
            List<String> consistencyProof = verifiableLog.consistencyProof(totalEntries1, totalEntries2).stream()
                    .map(this::bytesToString).collect(Collectors.toList());

            return new ConsistencyProof(consistencyProof);
        });
    }

    protected void insertEntries(Iterable<Entry> entries) {
        useHandle(handle -> {
            EntryDAO entryDAO = handle.attach(EntryDAO.class);
            entryDAO.insertInBatch(entries);
            entryDAO.setEntryNumber(entryDAO.currentEntryNumber() + Iterables.size(entries));
        });
    }

    protected void insertItems(Iterable<Item> items) {
        useHandle(handle -> handle.attach(ItemDAO.class).insertInBatch(items));
    }

    protected void insertCurrentKeys(List<CurrentKey> currentKeys) {
        useHandle(handle -> {
            CurrentKeysUpdateDAO currentKeysUpdateDAO = handle.attach(CurrentKeysUpdateDAO.class);

            int noOfRecordsDeleted = currentKeysUpdateDAO.removeRecordWithKeys(Lists.transform(currentKeys, r -> r.getKey()));
            currentKeysUpdateDAO.writeCurrentKeys(currentKeys);
            currentKeysUpdateDAO.updateTotalRecords(currentKeys.size() - noOfRecordsDeleted);
        });
    }

    private VerifiableLog getVerifiableLog(Handle handle) {
        EntryMerkleLeafStore merkleLeafStore = new EntryMerkleLeafStore(handle.attach(EntryQueryDAO.class));
        return new VerifiableLog(DigestUtils.getSha256Digest(), merkleLeafStore, memoizationStore);
    }

    private String bytesToString(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes).toLowerCase();
    }

    protected abstract void useHandle(HandleConsumer callback);

    protected abstract <ReturnType> ReturnType withHandle(HandleCallback<ReturnType> callback);

    protected abstract <ReturnType> ReturnType inTransaction(TransactionCallback<ReturnType> callback);
}
