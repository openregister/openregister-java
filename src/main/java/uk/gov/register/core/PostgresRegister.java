package uk.gov.register.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.register.configuration.IndexFunctionConfiguration.IndexNames;
import uk.gov.register.db.Index;
import uk.gov.register.exceptions.AppendEntryException;
import uk.gov.register.exceptions.FieldDefinitionException;
import uk.gov.register.exceptions.NoSuchFieldException;
import uk.gov.register.exceptions.IndexingException;
import uk.gov.register.exceptions.ItemValidationException;
import uk.gov.register.exceptions.NoSuchItemException;
import uk.gov.register.exceptions.RegisterDefinitionException;
import uk.gov.register.exceptions.NoSuchRegisterException;
import uk.gov.register.indexer.function.IndexFunction;
import uk.gov.register.service.EnvironmentValidator;
import uk.gov.register.service.ItemValidator;
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
    private final Index index;
    private final RegisterId registerId;
    private final EntryLog entryLog;
    private final BlobStore blobStore;
    private final Map<EntryType,Collection<IndexFunction>> indexFunctionsByEntryType;
    private final ItemValidator itemValidator;
    private final EnvironmentValidator environmentValidator;

    private RegisterMetadata registerMetadata;
    private Map<String, Field> fieldsByName;

    private final String defaultIndexForTypeUser = IndexNames.RECORD;
    private final String defaultIndexForTypeSystem = IndexNames.METADATA;

    public PostgresRegister(RegisterId registerId,
                            EntryLog entryLog,
                            BlobStore blobStore,
                            Index index,
                            Map<EntryType,Collection<IndexFunction>> indexFunctionsByEntryType,
                            ItemValidator itemValidator,
                            EnvironmentValidator environmentValidator) {
        this.registerId = registerId;
        this.entryLog = entryLog;
        this.blobStore = blobStore;
        this.index = index;
        this.indexFunctionsByEntryType = indexFunctionsByEntryType;
        this.itemValidator = itemValidator;
        this.environmentValidator = environmentValidator;
    }

    //region Items

    @Override
    public void addItem(Blob blob) {
        blobStore.addItem(blob);
    }

    @Override
    public Optional<Blob> getBlob(HashValue hash) {
        return blobStore.getItem(hash);
    }

    @Override
    public Collection<Blob> getAllItems() {
        return blobStore.getAllItems();
    }

    @Override
    public Iterator<Blob> getItemIterator() {
        return blobStore.getUserItemIterator();
    }

    @Override
    public Iterator<Blob> getItemIterator(int start, int end) {
        return blobStore.getUserItemIterator(start, end);
    }

    @Override
    public Iterator<Blob> getSystemItemIterator() {
        return blobStore.getSystemItemIterator();
    }

    //endregion

    //region Entries

    @Override
    public void appendEntry(final Entry entry) throws AppendEntryException {
        try {
            List<Blob> referencedBlobs = getReferencedItems(entry);

            referencedBlobs.forEach(i -> {
                if (entry.getEntryType() == EntryType.user) {
                    itemValidator.validateItem(i.getContent(), this.getFieldsByName(), this.getRegisterMetadata());
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
        } catch (IndexingException | ItemValidationException | FieldDefinitionException | RegisterDefinitionException |
                NoSuchRegisterException | NoSuchFieldException | NoSuchItemException exception) {
            throw new AppendEntryException(entry, exception);
        }
    }

    @Override
    public Optional<Entry> getEntry(int entryNumber) {
        return entryLog.getEntry(entryNumber);
    }

    @Override
    public Collection<Entry> getEntries(int start, int limit) {
        return entryLog.getEntries(start, limit);
    }

    @Override
    public Collection<Entry> getAllEntries() {
        return entryLog.getAllEntries();
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
    public Collection<Entry> allEntriesOfRecord(String key) {
        return index.findAllEntriesOfRecordBy(key);
    }

    @Override
    public Optional<Instant> getLastUpdatedTime() {
        return entryLog.getLastUpdatedTime();
    }

    @Override
    public Iterator<Entry> getEntryIterator() {
        return entryLog.getEntryIterator(defaultIndexForTypeUser);
    }

    @Override
    public Iterator<Entry> getEntryIterator(int totalEntries1, int totalEntries2) {
        return entryLog.getEntryIterator(defaultIndexForTypeUser, totalEntries1, totalEntries2);
    }

    @Override
    public Iterator<Entry> getEntryIterator(String indexName) {
        return entryLog.getEntryIterator(indexName);
    }

    @Override
    public Iterator<Entry> getEntryIterator(String indexName, int totalEntries1, int totalEntries2) {
        return entryLog.getEntryIterator(indexName, totalEntries1, totalEntries2);
    }

    //endregion

    //region Indexes

    @Override
    public Optional<Record> getRecord(String key) {
        return index.getRecord(key, defaultIndexForTypeUser);
    }

    @Override
    public List<Record> getRecords(int limit, int offset) {
        return index.getRecords(limit, offset, defaultIndexForTypeUser);
    }

    @Override
    public int getTotalRecords() {
        return getTotalRecords(defaultIndexForTypeUser);
    }

    @Override
    public List<Record> max100RecordsFacetedByKeyValue(String key, String value) throws NoSuchFieldException {
        if (!getRegisterMetadata().getFields().contains(key)) {
            throw new NoSuchFieldException(registerId, key);
        }

        return index.findMax100RecordsByKeyValue(key, value);
    }

    @Override
    public Optional<Record> getRecord(String key, String indexName) {
        return index.getRecord(key, indexName);
    }

    @Override
    public List<Record> getRecords(int limit, int offset, String indexName) {
        return index.getRecords(limit, offset, indexName);
    }

    @Override
    public int getTotalRecords(String indexName) {
        return index.getTotalRecords(indexName);
    }

    //endregion

    //region Verifiable Log

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

    //endregion

    //region Metadata

    @Override
    public RegisterId getRegisterId() {
        return registerId;
    }

    @Override
    public Optional<String> getRegisterName() {
        return getMetadataField("register-name");
    }

    @Override
    public Optional<String> getCustodianName() {
        return getMetadataField("custodian");
    }

    @Override
    public RegisterMetadata getRegisterMetadata() throws NoSuchRegisterException {
        if (registerMetadata == null) {
            registerMetadata = getRecord("register:" + registerId.value(), defaultIndexForTypeSystem)
                    .map(r -> extractObjectFromRecord(r, RegisterMetadata.class))
                    .orElseThrow(() -> new NoSuchRegisterException(registerId));
        }

        return registerMetadata;
    }

    @Override
    public Map<String, Field> getFieldsByName() throws NoSuchRegisterException, NoSuchFieldException {
        if (fieldsByName == null) {
            RegisterMetadata registerMetadata = getRegisterMetadata();
            List<String> fieldNames = registerMetadata.getFields();
            fieldsByName = new LinkedHashMap<>();
            fieldNames.forEach(fieldName -> fieldsByName.put(fieldName, getField(fieldName)));
        }
        return fieldsByName;
    }

    //endregion

    private Field getField(String fieldName) throws NoSuchFieldException {
        return getRecord("field:" + fieldName, defaultIndexForTypeSystem)
                .map(record -> extractObjectFromRecord(record, Field.class))
                .orElseThrow(() -> new NoSuchFieldException(registerId, fieldName));
    }

    private <T> T extractObjectFromRecord(Record record, Class<T> clazz) {
        return extractObjectFromItem(record.getBlobs().get(0), clazz);
    }

    private <T> T extractObjectFromItem(Blob blob, Class<T> clazz) {
        try {
            JsonNode content = blob.getContent();
            return mapper.treeToValue(content, clazz);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Optional<String> getMetadataField(String fieldName) {
        return getRecord(fieldName, defaultIndexForTypeSystem).map(r -> r.getBlobs().get(0).getValue(fieldName).get());
    }

    public Map<EntryType, Collection<IndexFunction>> getIndexFunctionsByEntryType() {
        return indexFunctionsByEntryType;
    }

    private List<Blob> getReferencedItems(Entry entry) throws NoSuchItemException {
        return entry.getItemHashes().stream()
                .map(h -> blobStore.getItem(h).orElseThrow(
                        () -> new NoSuchItemException(h)))
                .collect(Collectors.toList());
    }
}
