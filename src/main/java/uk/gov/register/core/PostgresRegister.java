package uk.gov.register.core;

import com.google.common.collect.Lists;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.db.RecordIndex;
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
                            RecordIndex recordIndex,
                            EntryLog entryLog,
                            ItemStore itemStore,
                            DBI dbi) {
        this.recordIndex = recordIndex;
        registerName = registerNameConfig.getRegister();
        this.entryLog = entryLog;
        this.itemStore = itemStore;
        this.dbi = dbi;
    }

    @Override
    public void mintItems(Iterable<Item> items) {
        dbi.useTransaction(TransactionIsolationLevel.SERIALIZABLE, (handle, status) -> {
            itemStore.putItems(handle, items);
            AtomicInteger currentEntryNumber = new AtomicInteger(entryLog.getTotalEntries(handle));
            List<Record> records = StreamSupport.stream(items.spliterator(), false)
                    .map(item -> new Record(new Entry(currentEntryNumber.incrementAndGet(), item.getSha256hex(), Instant.now()), item))
                    .collect(Collectors.toList());
            entryLog.appendEntries(handle, Lists.transform(records, r -> r.entry));
            handle.attach(RecordIndex.class).updateRecordIndex(handle, registerName, records);
        });
    }

    @Override
    public Optional<Entry> getEntry(int entryNumber) {
        return dbi.withHandle(handle -> entryLog.getEntry(handle, entryNumber));
    }

    @Override
    public Optional<Item> getItemBySha256(String sha256hex) {
        return dbi.withHandle(h -> itemStore.getItemBySha256(h, sha256hex));
    }

    @Override
    public int getTotalEntries() {
        return dbi.withHandle(entryLog::getTotalEntries);
    }

    @Override
    public Collection<Entry> getEntries(int start, int limit) {
        return dbi.withHandle(h -> entryLog.getEntries(h, start, limit));
    }

    @Override
    public Optional<Record> getRecord(String key) {
        return dbi.withHandle(h -> recordIndex.getRecord(h, key));
    }

    @Override
    public Collection<Entry> allEntriesOfRecord(String key) {
        return dbi.withHandle(h -> recordIndex.findAllEntriesOfRecordBy(h, registerName, key));
    }

    @Override
    public List<Record> getRecords(int limit, int offset) {
        return dbi.withHandle(h -> recordIndex.getRecords(h, limit, offset));
    }

    @Override
    public Collection<Entry> getAllEntries() {
        return dbi.withHandle(entryLog::getAllEntries);
    }

    @Override
    public Collection<Item> getAllItems() {
        return dbi.withHandle(itemStore::getAllItems);
    }

    @Override
    public int getTotalRecords() {
        return dbi.withHandle(recordIndex::getTotalRecords);
    }

    @Override
    public Optional<Instant> getLastUpdatedTime() {
        return dbi.withHandle(entryLog::getLastUpdatedTime);
    }

    @Override
    public List<Record> max100RecordsFacetedByKeyValue(String key, String value) {
        return dbi.withHandle(h -> recordIndex.findMax100RecordsByKeyValue(h, key, value));
    }

    @Override
    public RegisterProof getRegisterProof() throws NoSuchAlgorithmException {
        return dbi.withHandle(entryLog::getRegisterProof);
    }

    @Override
    public EntryProof getEntryProof(int entryNumber, int totalEntries) {
        return dbi.withHandle(handle ->
                entryLog.getEntryProof(handle, entryNumber, totalEntries));
    }

    @Override
    public ConsistencyProof getConsistencyProof(int totalEntries1, int totalEntries2) {
        return dbi.withHandle(handle ->
                entryLog.getConsistencyProof(handle, totalEntries1, totalEntries2));
    }
}
