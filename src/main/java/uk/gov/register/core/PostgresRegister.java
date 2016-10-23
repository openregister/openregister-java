package uk.gov.register.core;

import com.google.common.collect.Lists;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.db.RecordIndex;
import uk.gov.register.store.BackingStoreDriver;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;

import javax.inject.Inject;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class PostgresRegister implements Register {
    private final RecordIndex recordIndex;

    private final String registerName;
    private final EntryLog entryLog;
    private final ItemStore itemStore;
    private final DBI dbi;

    @Inject
    public PostgresRegister(RegisterNameConfiguration registerNameConfig,
                            DBI dbi,
                            BackingStoreDriver backingStoreDriver) {
        registerName = registerNameConfig.getRegister();
        this.entryLog = new EntryLog(backingStoreDriver);
        this.itemStore = new ItemStore(backingStoreDriver);
        this.recordIndex = new RecordIndex(backingStoreDriver);
        this.dbi = dbi;
    }

    @Override
    public void mintItems(Iterable<Item> items) {
        dbi.useTransaction(TransactionIsolationLevel.SERIALIZABLE, (handle, status) -> {
            itemStore.putItems(handle, items);
            AtomicInteger currentEntryNumber = new AtomicInteger(entryLog.getTotalEntries());
            List<Record> records = StreamSupport.stream(items.spliterator(), false)
                    .map(item -> new Record(new Entry(currentEntryNumber.incrementAndGet(), item.getSha256hex(), Instant.now()), item))
                    .collect(Collectors.toList());
            entryLog.appendEntries(handle, Lists.transform(records, r -> r.entry));
            handle.attach(RecordIndex.class).updateRecordIndex(handle, registerName, records);
        });
    }

    @Override
    public Optional<Entry> getEntry(int entryNumber) {
        return entryLog.getEntry(entryNumber);
    }

    @Override
    public Optional<Item> getItemBySha256(String sha256hex) {
        return itemStore.getItemBySha256(sha256hex);
    }

    @Override
    public int getTotalEntries() {
        return entryLog.getTotalEntries();
    }

    @Override
    public Collection<Entry> getEntries(int start, int limit) {
        return entryLog.getEntries(start, limit);
    }

    @Override
    public Optional<Record> getRecord(String key) {
        return recordIndex.getRecord(key);
    }

    @Override
    public Collection<Entry> allEntriesOfRecord(String key) {
        return recordIndex.findAllEntriesOfRecordBy(registerName, key);
    }

    @Override
    public List<Record> getRecords(int limit, int offset) {
        return recordIndex.getRecords(limit, offset);
    }

    @Override
    public Collection<Entry> getAllEntries() {
        return entryLog.getAllEntries();
    }

    @Override
    public Collection<Item> getAllItems() {
        return itemStore.getAllItems();
    }

    @Override
    public int getTotalRecords() {
        return recordIndex.getTotalRecords();
    }

    @Override
    public Optional<Instant> getLastUpdatedTime() {
        return entryLog.getLastUpdatedTime();
    }

    @Override
    public List<Record> max100RecordsFacetedByKeyValue(String key, String value) {
        return recordIndex.findMax100RecordsByKeyValue(key, value);
    }

    @Override
    public RegisterProof getRegisterProof() throws NoSuchAlgorithmException {
        return entryLog.getRegisterProof();
    }

    @Override
    public EntryProof getEntryProof(int entryNumber, int totalEntries) {
        return entryLog.getEntryProof(entryNumber, totalEntries);
    }

    @Override
    public ConsistencyProof getConsistencyProof(int totalEntries1, int totalEntries2) {
        return entryLog.getConsistencyProof(totalEntries1, totalEntries2);
    }
}
