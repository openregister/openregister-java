package uk.gov.register.store.postgres;

import uk.gov.register.core.*;
import uk.gov.register.db.*;
import uk.gov.register.exceptions.IndexingException;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.util.HashValue;

import java.util.*;
import java.util.stream.Collectors;

public class PostgresDataAccessLayer extends PostgresReadDataAccessLayer implements DataAccessLayer {
    private final List<Entry> stagedEntries;
    private final Map<HashValue, Item> stagedItems;

    private final EntryDAO entryDAO;
    private final ItemDAO itemDAO;

    public PostgresDataAccessLayer(
            EntryQueryDAO entryQueryDAO, EntryDAO entryDAO, ItemQueryDAO itemQueryDAO,
            ItemDAO itemDAO, RecordQueryDAO recordQueryDAO, String schema) {
        super(entryQueryDAO, itemQueryDAO, recordQueryDAO, schema);
        this.entryDAO = entryDAO;
        this.itemDAO = itemDAO;

        stagedEntries = new ArrayList<>();
        stagedItems = new HashMap<>();
    }

    @Override
    public void appendEntry(Entry entry) throws IndexingException {
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
    public void addItem(Item item) {
        stagedItems.put(item.getSha256hex(), item);
    }

    @Override
    public Optional<Item> getItem(HashValue hash) {
        if (stagedItems.containsKey(hash)) {
            return Optional.of(stagedItems.get(hash));
        }
        
        return super.getItem(hash);
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

        insertEntriesInBatch(EntryType.user, "entry");
        insertEntriesInBatch(EntryType.system, "entry_system");
        entryDAO.setEntryNumber(entryQueryDAO.getTotalEntries(schema) + stagedEntries.stream().filter(e -> e.getEntryType().equals(EntryType.user)).collect(Collectors.toList()).size(), schema);
        stagedEntries.clear();
    }

    private void insertEntriesInBatch(EntryType entryType, String entryTableName) {
        List<Entry> entries = stagedEntries.stream().filter(e -> e.getEntryType().equals(entryType)).collect(Collectors.toList());

        entryDAO.insertInBatch(entries.stream().filter(e -> e.getEntryType().equals(entryType)).collect(Collectors.toList()), schema, entryTableName);
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
