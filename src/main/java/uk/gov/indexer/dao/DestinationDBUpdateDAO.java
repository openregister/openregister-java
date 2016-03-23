package uk.gov.indexer.dao;

import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public abstract class DestinationDBUpdateDAO implements GetHandle, DBConnectionDAO {
    private final CurrentKeysUpdateDAO currentKeysUpdateDAO;
    private final IndexedEntriesUpdateDAO indexedEntriesUpdateDAO;
    private final EntryUpdateDAO entryUpdateDAO;
    private final ItemUpdateDAO itemUpdateDAO;

    public DestinationDBUpdateDAO() {
        Handle handle = getHandle();

        currentKeysUpdateDAO = handle.attach(CurrentKeysUpdateDAO.class);
        currentKeysUpdateDAO.ensureRecordTablesInPlace();

        indexedEntriesUpdateDAO = handle.attach(IndexedEntriesUpdateDAO.class);
        indexedEntriesUpdateDAO.ensureEntryTablesInPlace();

        if (!indexedEntriesUpdateDAO.indexedEntriesIndexExists()) {
            indexedEntriesUpdateDAO.createIndexedEntriesIndex();
        }

        entryUpdateDAO = handle.attach(EntryUpdateDAO.class);
        entryUpdateDAO.ensureEntryTableInPlace();

        itemUpdateDAO = handle.attach(ItemUpdateDAO.class);
        itemUpdateDAO.ensureItemTableInPlace();
        itemUpdateDAO.ensureItemIndexInPlace();
    }

    // TODO: Remove once migration to new schema complete
    public int lastReadSerialNumber() {
        return indexedEntriesUpdateDAO.lastReadSerialNumber();
    }

    public int lastReadEntryNumber() {
        return entryUpdateDAO.lastReadEntryNumber();
    }

    @Transaction(TransactionIsolationLevel.SERIALIZABLE)
    public void writeEntriesInBatch(String registerName, List<FatEntry> entries) {
        List<OrderedEntryIndex> orderedEntryIndex = entries.stream().map(FatEntry::dbEntry).collect(Collectors.toList());

        indexedEntriesUpdateDAO.writeBatch(orderedEntryIndex);

        upsertInCurrentKeysTable(registerName, orderedEntryIndex);
        indexedEntriesUpdateDAO.updateTotalEntries(orderedEntryIndex.size());
    }

    @Transaction(TransactionIsolationLevel.SERIALIZABLE)
    public void writeEntriesAndItemsInBatch(List<Entry> entries, List<Item> items) {
        Set<Item> newItems = extractNewItems(items);

        entryUpdateDAO.writeBatch(entries);
        if (!newItems.isEmpty()) {
            itemUpdateDAO.writeBatch(newItems);
        }
    }

    private void upsertInCurrentKeysTable(String registerName, List<OrderedEntryIndex> orderedEntryIndexes) {
        int noOfRecordsDeleted = currentKeysUpdateDAO.removeRecordWithKeys(Lists.transform(orderedEntryIndexes, e -> getKey(registerName, e.getEntry())));
        List<CurrentKey> currentKeys = extractCurrentKeys(registerName, orderedEntryIndexes);

        currentKeysUpdateDAO.writeCurrentKeys(currentKeys);
        currentKeysUpdateDAO.updateTotalRecords(currentKeys.size() - noOfRecordsDeleted);
    }

    private List<CurrentKey> extractCurrentKeys(String registerName, List<OrderedEntryIndex> orderedEntryIndexes) {
        Map<String, Integer> currentKeys = new HashMap<>();
        orderedEntryIndexes.forEach(e1 -> currentKeys.put(getKey(registerName, e1.getEntry()), e1.getSerial_number()));
        return currentKeys
                .entrySet()
                .stream()
                .map(e -> new CurrentKey(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private String getKey(String registerName, String entry) {
        JsonNode jsonNode = Jackson.jsonNodeOf(entry);
        return jsonNode.get("entry").get(registerName).textValue();
    }

    private Set<Item> extractNewItems(List<Item> items) {
        List<String> existingItemHexValues = itemUpdateDAO.getExistingItemHexValues(Lists.transform(items, Item::getSha256hex));
        return items.stream().filter(i -> !existingItemHexValues.contains(i.getSha256hex())).collect(Collectors.toSet());
    }
}
