package uk.gov.register.store.postgres;

import com.google.common.collect.Iterables;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.db.*;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.util.EntryItemPair;
import uk.gov.register.util.HashValue;

import java.util.*;
import java.util.stream.IntStream;

public class PostgresDataAccessLayer extends PostgresReadDataAccessLayer implements DataAccessLayer {
    private final List<Entry> stagedEntries;
    private final Map<HashValue, Item> stagedItems;
    private final HashMap<String, Integer> stagedCurrentKeys;
    private final Set<String> entriesWithoutItems;

    private final EntryDAO entryDAO;
    private final EntryItemDAO entryItemDAO;
    private final ItemDAO itemDAO;
    private final CurrentKeysUpdateDAO currentKeysDAO;

    public PostgresDataAccessLayer(
            EntryQueryDAO entryQueryDAO, IndexQueryDAO indexQueryDAO, EntryDAO entryDAO,
            EntryItemDAO entryItemDAO, ItemQueryDAO itemQueryDAO,
            ItemDAO itemDAO, RecordQueryDAO recordQueryDAO, CurrentKeysUpdateDAO currentKeysUpdateDAO) {
        super(entryQueryDAO, indexQueryDAO, itemQueryDAO, recordQueryDAO);
        this.entryDAO = entryDAO;
        this.entryItemDAO = entryItemDAO;
        this.itemDAO = itemDAO;
        this.currentKeysDAO = currentKeysUpdateDAO;

        stagedEntries = new ArrayList<>();
        stagedItems = new HashMap<>();
        stagedCurrentKeys = new HashMap<>();
        entriesWithoutItems = new HashSet<>();
    }

    @Override
    public void appendEntry(Entry entry) {
        stagedEntries.add(entry);
    }

    @Override
    public int getTotalEntries() {
        // This method is called a lot, so we want to avoid checkpointing
        // every time it's called.  Instead we compute the result from stagedEntries,
        // falling back to the DB if necessary.
        OptionalInt maxStagedEntryNumber = getMaxStagedEntryNumber();
        return maxStagedEntryNumber.orElseGet(super::getTotalEntries);
    }

    @Override
    public void putItem(Item item) {
        stagedItems.put(item.getSha256hex(), item);
    }

    @Override
    public void updateRecordIndex(Entry entry) {
        stagedCurrentKeys.put(entry.getKey(), entry.getEntryNumber());

        if (entry.getItemHashes().isEmpty()) {
            entriesWithoutItems.add(entry.getKey());
        }
    }

    @Override
    public void checkpoint() {
        writeStagedEntriesToDatabase();
        writeStagedItemsToDatabase();
        writeStagedCurrentKeysToDatabase();
    }

    private void writeStagedEntriesToDatabase() {
        if (stagedEntries.isEmpty()) {
            return;
        }

        List<EntryItemPair> entryItemPairs = new ArrayList<>();
        stagedEntries.forEach(se -> se.getItemHashes().forEach(h -> entryItemPairs.add(new EntryItemPair(se.getEntryNumber(), h))));

        entryDAO.insertInBatch(stagedEntries);
        entryItemDAO.insertInBatch(entryItemPairs);
        entryDAO.setEntryNumber(entryDAO.currentEntryNumber() + stagedEntries.size());
        stagedEntries.clear();
    }

    private void writeStagedItemsToDatabase() {
        if (stagedItems.isEmpty()) {
            return;
        }
        itemDAO.insertInBatch(stagedItems.values());
        stagedItems.clear();
    }

    private void writeStagedCurrentKeysToDatabase() {
        int noOfRecordsDeleted = removeRecordsWithKeys(stagedCurrentKeys.keySet());

        currentKeysDAO.writeCurrentKeys(Iterables.transform(stagedCurrentKeys.entrySet(),
                keyValue -> new CurrentKey(keyValue.getKey(), keyValue.getValue()))
        );

        currentKeysDAO.updateTotalRecords(stagedCurrentKeys.size() - noOfRecordsDeleted - entriesWithoutItems.size());
        stagedCurrentKeys.clear();
        entriesWithoutItems.clear();
    }

    private OptionalInt getMaxStagedEntryNumber() {
        if (stagedEntries.isEmpty()) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(stagedEntries.get(stagedEntries.size() - 1).getEntryNumber());
    }

    private int removeRecordsWithKeys(Iterable<String> keySet) {
        int[] noOfRecordsDeletedPerBatch = currentKeysDAO.removeRecordWithKeys(keySet);
        return IntStream.of(noOfRecordsDeletedPerBatch).sum();
    }
}
