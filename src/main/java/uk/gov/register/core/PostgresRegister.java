package uk.gov.register.core;

import com.google.common.collect.Lists;
import uk.gov.register.db.RecordIndex;
import uk.gov.register.exceptions.NoSuchFieldException;
import uk.gov.register.exceptions.NoSuchItemForEntryException;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.store.BackingStoreDriver;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;

import javax.inject.Inject;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Iterator;

public class PostgresRegister implements Register {
    private final RecordIndex recordIndex;

    private final String registerName;
    private final EntryLog entryLog;
    private final ItemStore itemStore;
    private final ArrayList<String> fields;

    @Inject
    public PostgresRegister(RegisterData registerData,
                            BackingStoreDriver backingStoreDriver,
                            ItemValidator itemValidator) {
        RegisterMetadata registerMetadata = registerData.getRegister();
        registerName = registerMetadata.getRegisterName();
        fields = Lists.newArrayList(registerMetadata.getFields());
        this.entryLog = new EntryLog(backingStoreDriver);
        this.itemStore = new ItemStore(backingStoreDriver, itemValidator, registerName);
        this.recordIndex = new RecordIndex(backingStoreDriver);
    }

    @Override
    public void putItem(Item item) {
        itemStore.putItem(item);
    }

    @Override
    public void appendEntry(Entry entry) {
        Item item = itemStore.getItemBySha256(entry.getSha256hex()).orElseThrow(() -> new NoSuchItemForEntryException(entry));
        entryLog.appendEntry(entry);
        recordIndex.updateRecordIndex(registerName, new Record(entry, item));
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
    public boolean containsField(String fieldName) {
        return fields.contains(fieldName);
    }

    @Override
    public List<Record> max100RecordsFacetedByKeyValue(String key, String value) {
        if (!containsField(key)) {
            throw new NoSuchFieldException(registerName, key);
        }

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

    @Override
    public Iterator<Entry> getEntryIterator(int start, int end) {
        return entryLog.getIterator(start, end);
    }

    @Override
    public Iterator<Item> getItemIterator(int start, int end) {
        return itemStore.getIterator(start, end);
    }
}
