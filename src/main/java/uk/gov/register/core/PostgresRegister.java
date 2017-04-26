package uk.gov.register.core;

import uk.gov.register.configuration.RegisterFieldsConfiguration;
import uk.gov.register.db.DerivationRecordIndex;
import uk.gov.register.db.IndexDAO;
import uk.gov.register.db.IndexQueryDAO;
import uk.gov.register.exceptions.NoSuchFieldException;
import uk.gov.register.indexer.IndexDriver;
import uk.gov.register.indexer.function.CurrentCountriesIndexFunction;
import uk.gov.register.indexer.function.IndexFunction;
import uk.gov.register.indexer.function.LocalAuthorityByTypeIndexFunction;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;

import java.time.Instant;
import java.util.*;

public class PostgresRegister implements Register {
    private final RecordIndex recordIndex;
    private final DerivationRecordIndex derivationRecordIndex;
    private final RegisterName registerName;
    private final EntryLog entryLog;
    private final ItemStore itemStore;
    private final RegisterFieldsConfiguration registerFieldsConfiguration;
    private final RegisterMetadata registerMetadata;
    private final IndexDriver indexDriver;
    private final List<IndexFunction> indexFunctions;

    public PostgresRegister(RegisterMetadata registerMetadata,
                            RegisterFieldsConfiguration registerFieldsConfiguration,
                            EntryLog entryLog,
                            ItemStore itemStore,
                            RecordIndex recordIndex,
                            IndexDAO indexDAO,
                            IndexQueryDAO indexQueryDAO,
                            DerivationRecordIndex derivationRecordIndex) {
        registerName = registerMetadata.getRegisterName();
        this.entryLog = entryLog;
        this.itemStore = itemStore;
        this.recordIndex = recordIndex;
        this.derivationRecordIndex = derivationRecordIndex;
        this.registerFieldsConfiguration = registerFieldsConfiguration;
        this.registerMetadata = registerMetadata;
        this.indexDriver = new IndexDriver(this, indexDAO, indexQueryDAO);

        this.indexFunctions = new ArrayList<>();

        if (this.registerMetadata.getRegisterName().value().equals("country")) {
            this.indexFunctions.add(new CurrentCountriesIndexFunction(this));
        }
        else if (this.registerMetadata.getRegisterName().value().equals("local-authority-eng")) {
            this.indexFunctions.add(new LocalAuthorityByTypeIndexFunction(this));
        }
    }

    @Override
    public void putItem(Item item) {
        itemStore.putItem(item);
    }

    @Override
    public void appendEntry(Entry entry) {
        entryLog.appendEntry(entry);

        for (IndexFunction indexFunction : indexFunctions) {
            indexDriver.indexEntry(entry, indexFunction);
        }

        recordIndex.updateRecordIndex(entry);
    }

    public void commit() {
        itemStore.checkpoint();
        entryLog.checkpoint();
        recordIndex.checkpoint();
    }

    @Override
    public Optional<Entry> getEntry(int entryNumber) {
        return entryLog.getEntry(entryNumber);
    }

    @Override
    public Optional<Item> getItemBySha256(HashValue hash) {
        return itemStore.getItemBySha256(hash);
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
        return recordIndex.findAllEntriesOfRecordBy(key);
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
        return registerFieldsConfiguration.containsField(fieldName);
    }

    @Override
    public List<Record> max100RecordsFacetedByKeyValue(String key, String value) {
        if (!registerFieldsConfiguration.containsField(key)) {
            throw new NoSuchFieldException(registerName, key);
        }

        return recordIndex.findMax100RecordsByKeyValue(key, value);
    }

    @Override
    public RegisterProof getRegisterProof() {
        return entryLog.getRegisterProof();
    }

    @Override
    public RegisterProof getRegisterProof(int totalEntries) {
        return entryLog.getRegisterProof(totalEntries);
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
    public Iterator<Entry> getEntryIterator() {
        return entryLog.getIterator();
    }

    @Override
    public Iterator<Entry> getEntryIterator(int totalEntries1, int totalEntries2) {
        return entryLog.getIterator(totalEntries1, totalEntries2);
    }

    @Override
    public Iterator<Item> getItemIterator() {
        return itemStore.getIterator();
    }

    @Override
    public Iterator<Item> getItemIterator(int start, int end) {
        return itemStore.getIterator(start, end);
    }

    @Override
    public Iterator<Entry> getDerivationEntryIterator(String indexName) {
        return entryLog.getDerivationIterator(indexName);
    }

    @Override
    public Iterator<Entry> getDerivationEntryIterator(String indexName, int totalEntries1, int totalEntries2) {
        return entryLog.getDerivationIterator(indexName, totalEntries1, totalEntries2);
    }

    @Override
    public RegisterName getRegisterName() {
        return registerName;
    }

    @Override
    public RegisterMetadata getRegisterMetadata() {
        return registerMetadata;
    }

    @Override
    public Optional<Record> getDerivationRecord(String key, String derivationName) {
        return derivationRecordIndex.getRecord(key, derivationName);
    }

    @Override
    public List<Record> getDerivationRecords(int limit, int offset, String derivationName) {
        return derivationRecordIndex.getRecords(limit, offset, derivationName);
    }

    @Override
    public int getTotalDerivationRecords(String derivationName) {
        return derivationRecordIndex.getTotalRecords(derivationName);
    }

}
