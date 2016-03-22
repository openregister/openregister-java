package uk.gov.indexer.dao;

import com.google.common.collect.Lists;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;

import java.util.List;
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
    public void writeEntriesAndItemsInBatch(String registerName, List<Entry> entries, List<Item> items) {
        List<String> existingItemHexValues = itemUpdateDAO.getExistingItemHexValues(Lists.transform(items, Item::getItemHash));
        List<Item> newItems = items.stream().filter(i -> !existingItemHexValues.contains(i.getItemHash())).collect(Collectors.toList());

        entryUpdateDAO.writeBatch(entries);
        if (!newItems.isEmpty()) {
            itemUpdateDAO.writeBatch(newItems);
        }

        if (lastReadEntryNumber() > lastReadSerialNumber()) {
            upsertInCurrentKeysTable(registerName, entries, items);
            indexedEntriesUpdateDAO.updateTotalEntries(entries.size());
        }
    }

    // TODO: Remove once migration to new schema complete
    private void upsertInCurrentKeysTable(String registerName, List<OrderedEntryIndex> orderedEntryIndexes) {
        List<CurrentKey> currentKeys = CurrentKeyTransformer.extractCurrentKeys(registerName, orderedEntryIndexes);
        upsertInCurrentKeysTable(currentKeys);
    }

    private void upsertInCurrentKeysTable(String registerName, List<Entry> entries, List<Item> items) {
        List<CurrentKey> currentKeys = CurrentKeyTransformer.extractCurrentKeys(registerName, entries, items);
        upsertInCurrentKeysTable(currentKeys);
    }

    private void upsertInCurrentKeysTable(List<CurrentKey> currentKeys) {
        int noOfRecordsDeleted = currentKeysUpdateDAO.removeRecordWithKeys(Lists.transform(currentKeys, ck -> ck.getKey()));

        currentKeysUpdateDAO.writeCurrentKeys(currentKeys);
        currentKeysUpdateDAO.updateTotalRecords(currentKeys.size() - noOfRecordsDeleted);
    }
}
