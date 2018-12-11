package uk.gov.register.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.register.db.RecordSet;
import uk.gov.register.exceptions.AppendEntryException;
import uk.gov.register.exceptions.FieldDefinitionException;
import uk.gov.register.exceptions.NoSuchFieldException;
import uk.gov.register.exceptions.IndexingException;
import uk.gov.register.exceptions.ItemValidationException;
import uk.gov.register.exceptions.NoSuchItemException;
import uk.gov.register.exceptions.RegisterDefinitionException;
import uk.gov.register.exceptions.NoSuchRegisterException;
import uk.gov.register.service.EnvironmentValidator;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;

import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.*;

public class RegisterImpl implements Register {
    private static ObjectMapper mapper = new ObjectMapper();
    private final RecordSet recordSet;
    private final RegisterId registerId;
    private final EntryLog entryLog;
    private final ItemStore itemStore;
    private final ItemValidator itemValidator;
    private final EnvironmentValidator environmentValidator;

    private RegisterMetadata registerMetadata;
    private Map<String, Field> fieldsByName;

    public RegisterImpl(RegisterId registerId,
                        EntryLog entryLog,
                        ItemStore itemStore,
                        RecordSet recordSet,
                        ItemValidator itemValidator,
                        EnvironmentValidator environmentValidator) {
        this.registerId = registerId;
        this.entryLog = entryLog;
        this.itemStore = itemStore;
        this.recordSet = recordSet;
        this.itemValidator = itemValidator;
        this.environmentValidator = environmentValidator;
    }

    //region Items

    @Override
    public void addItem(Item item) {
        itemStore.addItem(item);
    }

    @Override
    public Optional<Item> getItemByV1Hash(HashValue hash) {
        return itemStore.getItemByV1Hash(hash);
    }

    @Override
    public Optional<Item> getItem(HashValue hash) {
        return itemStore.getItem(hash);
    }

    @Override
    public Collection<Item> getAllItems() {
        return itemStore.getAllItems();
    }

    @Override
    public Collection<Item> getAllItems(EntryType entryType) {
        return itemStore.getAllItems(entryType);
    }

    @Override
    public Iterator<Item> getItemIterator(EntryType entryType) {
        return itemStore.getItemIterator(entryType);
    }

    @Override
    public Iterator<Item> getItemIterator(int start, int end) {
        return itemStore.getUserItemIterator(start, end);
    }

    @Override
    public Collection<Item> getUserItemsPaginated(Optional<HashValue> start, int limit) {
        return itemStore.getUserItemsPaginated(start, limit);
    }

    //endregion

    //region Entries

    @Override
    public void appendEntry(final Entry entry) throws AppendEntryException {
        try {
            Item item = getReferencedItem(entry);

            if (entry.getEntryType() == EntryType.user) {
                itemValidator.validateItem(item.getContent(), this.getFieldsByName(), this.getRegisterMetadata());
            } else if (entry.getKey().startsWith("field:")) {
                Field field = extractObjectFromItem(item, Field.class);
                environmentValidator.validateFieldAgainstEnvironment(field);
            } else if (entry.getKey().startsWith("register:")) {
                RegisterMetadata localRegisterMetadata = this.extractObjectFromItem(item, RegisterMetadata.class);
                // will throw exception if field not present
                localRegisterMetadata.getFields().forEach(this::getField);
                environmentValidator.validateRegisterAgainstEnvironment(localRegisterMetadata);
            }

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
    public int getTotalEntries(EntryType entryType) {
        return entryLog.getTotalEntries(entryType);
    }

    @Override
    public Collection<Entry> allEntriesOfRecord(String key) {
        return recordSet.findAllEntriesOfRecordBy(key);
    }

    @Override
    public Optional<Instant> getLastUpdatedTime() {
        return entryLog.getLastUpdatedTime();
    }

    @Override
    public Iterator<Entry> getEntryIterator(int totalEntries1, int totalEntries2) {
        return entryLog.getEntryIterator(EntryType.user, totalEntries1, totalEntries2);
    }

    @Override
    public Iterator<Entry> getEntryIterator(EntryType entryType) {
        return entryLog.getEntryIterator(entryType);
    }

    //endregion

    //region Records

    @Override
    public List<Record> max100RecordsFacetedByKeyValue(String key, String value) throws NoSuchFieldException {
        if (!getRegisterMetadata().getFields().contains(key)) {
            throw new NoSuchFieldException(registerId, key);
        }

        return recordSet.findMax100RecordsByKeyValue(key, value);
    }

    @Override
    public Optional<Record> getRecord(EntryType entryType, String key) {
        return recordSet.getRecord(entryType, key);
    }

    @Override
    public List<Record> getRecords(EntryType entryType, int limit, int offset) {
        return recordSet.getRecords(entryType, limit, offset);
    }

    @Override
    public int getTotalRecords(EntryType entryType) {
        return recordSet.getTotalRecords(entryType);
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
            registerMetadata = getRecord(EntryType.system, "register:" + registerId.value())
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
        return getRecord(EntryType.system, "field:" + fieldName)
                .map(record -> extractObjectFromRecord(record, Field.class))
                .orElseThrow(() -> new NoSuchFieldException(registerId, fieldName));
    }

    private <T> T extractObjectFromRecord(Record record, Class<T> clazz) {
        return extractObjectFromItem(record.getItem(), clazz);
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
        return getRecord(EntryType.system, fieldName).map(r -> r.getItem().getValue(fieldName).get());
    }

    private Item getReferencedItem(Entry entry) throws NoSuchItemException {
        return itemStore.getItemByV1Hash(entry.getV1ItemHash()).orElseThrow(() -> new NoSuchItemException(entry.getV1ItemHash()));
    }
}
