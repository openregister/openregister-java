package uk.gov.register.store.postgres;

import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Item;
import uk.gov.register.db.*;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.util.EntryItemPair;
import uk.gov.register.util.HashValue;

import java.util.*;
import java.util.stream.Collectors;

public class PostgresDataAccessLayer extends PostgresReadDataAccessLayer implements DataAccessLayer {
    private final List<Entry> stagedEntries;
    private final Map<HashValue, Item> stagedItems;
    private final HashMap<String, Integer> stagedCurrentKeys;
    private final Set<String> entriesWithoutItems;

    private final EntryDAO entryDAO;
    private final EntryItemDAO entryItemDAO;
    private final ItemDAO itemDAO;
    private final IndexDAO indexDAO;

    public PostgresDataAccessLayer(
            EntryQueryDAO entryQueryDAO, IndexDAO indexDAO, IndexQueryDAO indexQueryDAO, EntryDAO entryDAO,
            EntryItemDAO entryItemDAO, ItemQueryDAO itemQueryDAO,
            ItemDAO itemDAO, RecordQueryDAO recordQueryDAO, String schema) {
        super(entryQueryDAO, indexQueryDAO, itemQueryDAO, recordQueryDAO, schema);
        this.entryDAO = entryDAO;
        this.entryItemDAO = entryItemDAO;
        this.itemDAO = itemDAO;
        this.indexDAO = indexDAO;

        stagedEntries = new ArrayList<>();
        stagedItems = new HashMap<>();
        stagedCurrentKeys = new HashMap<>();
        entriesWithoutItems = new HashSet<>();
    }

    @Override
    public void appendEntry(Entry entry) {
        stagedEntries.add(entry);

        if (entry.getEntryType().equals(EntryType.system)) {
            checkpoint();
        }
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
    public int getTotalEntries(EntryType entryType) {
        if (entryType.equals(EntryType.system)) {
            return super.getTotalSystemEntries();
        }
        return getTotalEntries();
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
    public void start(String indexName, String key, String itemHash, int startEntryNumber, int startIndexEntryNumber) {
        indexDAO.start(indexName, key, itemHash, startEntryNumber, startIndexEntryNumber, schema);
    }

    @Override
    public void end(String indexName, String entryKey, String indexKey, String itemHash, int endEntryNumber, int endIndexEntryNumber) {
        indexDAO.end(indexName, entryKey, indexKey, itemHash, endEntryNumber, endIndexEntryNumber, schema, "entry");
    }

    @Override
    public Optional<Item> getItemBySha256(HashValue hash) {
        if (stagedItems.containsKey(hash)) {
            return Optional.of(stagedItems.get(hash));
        }
        return super.getItemBySha256(hash);
    }

    @Override
    public void checkpoint() {
        writeStagedEntriesToDatabase();
        writeStagedItemsToDatabase();
    }

    private void writeStagedEntriesToDatabase() {
        if (stagedEntries.isEmpty()) {
            return;
        }

        insertEntriesInBatch(EntryType.user, "entry", "entry_item");
        insertEntriesInBatch(EntryType.system, "entry_system", "entry_item_system");
        entryDAO.setEntryNumber(entryDAO.currentEntryNumber(schema) + stagedEntries.stream().filter(e -> e.getEntryType().equals(EntryType.user)).collect(Collectors.toList()).size(), schema);
        stagedEntries.clear();
    }

    private void insertEntriesInBatch(EntryType entryType, String entryTableName, String entryItemTableName) {
        List<Entry> entries = stagedEntries.stream().filter(e -> e.getEntryType().equals(entryType)).collect(Collectors.toList());

        entryDAO.insertInBatch(entries.stream().filter(e -> e.getEntryType().equals(entryType)).collect(Collectors.toList()), schema, entryTableName);
        entryItemDAO.insertInBatch(entries.stream()
                .filter(e -> e.getEntryType().equals(entryType))
                .flatMap(e -> e.getItemHashes().stream().map(i -> new EntryItemPair(e.getEntryNumber(), i)))
                .collect(Collectors.toList()), schema, entryItemTableName);
    }

    private void writeStagedItemsToDatabase() {
        if (stagedItems.isEmpty()) {
            return;
        }
        itemDAO.insertInBatch(stagedItems.values(), schema);
        stagedItems.clear();
    }
    
    private OptionalInt getMaxStagedEntryNumber() {
        if (stagedEntries.isEmpty()) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(stagedEntries.get(stagedEntries.size() - 1).getEntryNumber());
    }
}
