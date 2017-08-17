package uk.gov.register.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.register.configuration.IndexFunctionConfiguration.IndexNames;
import uk.gov.register.db.DerivationRecordIndex;
import uk.gov.register.exceptions.FieldUndefinedException;
import uk.gov.register.exceptions.NoSuchFieldException;
import uk.gov.register.exceptions.RegisterUndefinedException;
import uk.gov.register.exceptions.SerializationFormatValidationException;
import uk.gov.register.indexer.function.IndexFunction;
import uk.gov.register.service.EnvironmentValidator;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.util.FieldComparer;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;

import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class PostgresRegister implements Register {
    private static ObjectMapper mapper = new ObjectMapper();
    private final RecordIndex recordIndex;
    private final DerivationRecordIndex derivationRecordIndex;
    private final RegisterName registerName;
    private final EntryLog entryLog;
    private final ItemStore itemStore;
    private final Map<EntryType,Collection<IndexFunction>> indexFunctionsByEntryType;
    private final ItemValidator itemValidator;
    private final EnvironmentValidator environmentValidator;

    private RegisterMetadata registerMetadata;
    private Map<String, Field> fieldsByName;

    public PostgresRegister(RegisterName registerName,
                            EntryLog entryLog,
                            ItemStore itemStore,
                            RecordIndex recordIndex,
                            DerivationRecordIndex derivationRecordIndex,
                            Map<EntryType,Collection<IndexFunction>> indexFunctionsByEntryType,
                            ItemValidator itemValidator,
                            EnvironmentValidator environmentValidator) {
        this.registerName = registerName;
        this.entryLog = entryLog;
        this.itemStore = itemStore;
        this.recordIndex = recordIndex;
        this.derivationRecordIndex = derivationRecordIndex;
        this.indexFunctionsByEntryType = indexFunctionsByEntryType;
        this.itemValidator = itemValidator;
        this.environmentValidator = environmentValidator;
    }

    @Override
    public void putItem(Item item) {
        itemStore.putItem(item);
    }

    @Override
    public void appendEntry(final Entry entry) {
        List<Item> referencedItems = getReferencedItems(entry);

        referencedItems.forEach(i -> {
            if (entry.getEntryType() == EntryType.user) {
                itemValidator.validateItem(i.getContent(), this.getFieldsByName(), this.getRegisterMetadata());
                //TODO: Validate system entries

            } else if (entry.getKey().startsWith("field:")) {
                Field field = extractObjectFromItem(i, Field.class);
                environmentValidator.validateFieldAgainstEnvironment(field);
            } else if (entry.getKey().startsWith("register:")) {
                RegisterMetadata localRegisterMetadata = this.extractObjectFromItem(i, RegisterMetadata.class);
                // will throw exception if field not present
                localRegisterMetadata.getFields().forEach(this::getField);
                environmentValidator.validateRegisterAgainstEnvironment(localRegisterMetadata);
            }
        });

        entryLog.appendEntry(entry);
    }

    private List<Item> getReferencedItems(Entry entry) {
        return entry.getItemHashes().stream()
                .map(h -> itemStore.getItemBySha256(h).orElseThrow(
                        () -> new SerializationFormatValidationException("Failed to find item referenced by " + h.getValue())))
                .collect(Collectors.toList());
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
    public int getTotalEntries(EntryType entryType) {
        return entryLog.getTotalEntries(entryType);
    }

    @Override
    public Collection<Entry> getEntries(int start, int limit) {
        return entryLog.getEntries(start, limit);
    }

    @Override
    public Optional<Record> getRecord(String key) {
        return derivationRecordIndex.getRecord(key, IndexNames.RECORD);
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
        return getTotalDerivationRecords(IndexNames.RECORD);
    }

    @Override
    public Optional<Instant> getLastUpdatedTime() {
        return entryLog.getLastUpdatedTime();
    }

    @Override
    public List<Record> max100RecordsFacetedByKeyValue(String key, String value) {
        if (!getRegisterMetadata().getFields().contains(key)) {
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
    public Iterator<Item> getSystemItemIterator() {
        return itemStore.getSystemItemIterator();
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
    public Optional<String> getCustodianName() {
        return getMetadataField("custodian");
    }

    @Override
    public RegisterMetadata getRegisterMetadata() {
        if (registerMetadata == null) {
            registerMetadata = getDerivationRecord("register:" + registerName.value(), IndexNames.METADATA)
                    .map(r -> extractObjectFromRecord(r, RegisterMetadata.class))
                    .orElseThrow(() -> new RegisterUndefinedException(registerName));
        }

        return registerMetadata;
    }

    @Override
    public Optional<Record> getDerivationRecord(String key, String derivationName) {
        Optional<Record> foo = derivationRecordIndex.getRecord(key, derivationName);
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

    @Override
    public Map<String, Field> getFieldsByName() {
        if (fieldsByName == null) {
            RegisterMetadata registerMetadata = getRegisterMetadata();
            List<String> fieldNames = registerMetadata.getFields();
            fieldsByName = new LinkedHashMap<>();
            fieldNames.forEach(fieldName -> fieldsByName.put(fieldName, getField(fieldName)));
        }
        return fieldsByName;
    }

    private Field getField(String fieldName) {
        return getDerivationRecord("field:" + fieldName, IndexNames.METADATA)
                .map(record -> extractObjectFromRecord(record, Field.class))
                .orElseThrow(() -> new FieldUndefinedException(registerName, fieldName));
    }

    private <T> T extractObjectFromRecord(Record record, Class<T> clazz) {
        return extractObjectFromItem(record.getItems().get(0), clazz);
    }

    private <T> T extractObjectFromItem(Item item, Class<T> clazz) {
        try {
            JsonNode content = item.getContent();
            return mapper.treeToValue(content, clazz);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Optional<String> getMetadataField(String fieldName) {
        return getDerivationRecord(fieldName, IndexNames.METADATA).map(r -> r.getItems().get(0).getValue(fieldName).get());
    }

    public Map<EntryType, Collection<IndexFunction>> getIndexFunctionsByEntryType() {
        return indexFunctionsByEntryType;
    }
}
