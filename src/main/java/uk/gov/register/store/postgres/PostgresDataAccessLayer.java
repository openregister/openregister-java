package uk.gov.register.store.postgres;

import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.db.EntryDAO;
import uk.gov.register.proofs.EntryIterator;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.db.ItemDAO;
import uk.gov.register.db.ItemQueryDAO;
import uk.gov.register.db.RecordQueryDAO;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PostgresDataAccessLayer implements DataAccessLayer {
    private final EntryDAO entryDAO;
    private final EntryQueryDAO entryQueryDAO;
    private final ItemDAO itemDAO;
    private final ItemQueryDAO itemQueryDAO;
    private final RecordQueryDAO recordQueryDAO;
    private final String schema;
    
    public PostgresDataAccessLayer(
            EntryDAO entryDAO, EntryQueryDAO entryQueryDAO, ItemDAO itemDAO,
            ItemQueryDAO itemQueryDAO, RecordQueryDAO recordQueryDAO, String schema) {
        this.entryDAO = entryDAO;
        this.entryQueryDAO = entryQueryDAO;
        this.itemDAO = itemDAO;
        this.itemQueryDAO = itemQueryDAO;
        this.recordQueryDAO = recordQueryDAO;
        this.schema = schema;
    }

    // Entry Log

    @Override
    public void appendEntry(Entry entry) {
        appendEntries(Arrays.asList(entry));
    }

    @Override
    public Optional<Entry> getEntry(int entryNumber) {
        return entryQueryDAO.findByEntryNumber(entryNumber, schema);
    }

    @Override
    public Collection<Entry> getEntries(int start, int limit) {
        return entryQueryDAO.getEntries(start, limit, schema);
    }

    @Override
    public Collection<Entry> getAllEntries() {
        return entryQueryDAO.getAllEntriesNoPagination(schema);
    }

    @Override
    public Collection<Entry> getAllEntriesByKey(String key) {
        return entryQueryDAO.getAllEntriesByKey(key, schema);
    }

    @Override
    public Iterator<Entry> getEntryIterator(EntryType entryType) {
        return entryQueryDAO.getIterator(schema, getEntryTable(entryType));
    }

    @Override
    public Iterator<Entry> getEntryIterator(EntryType entryType, int totalEntries1, int totalEntries2) {
        return entryQueryDAO.getIterator(totalEntries1, totalEntries2, schema, getEntryTable(entryType));
    }

    @Override
    public <R> R withEntryIterator(Function<EntryIterator, R> callback) {
        return EntryIterator.withEntryIterator(entryQueryDAO, entryIterator -> callback.apply(entryIterator), schema);
    }

    @Override
    public int getTotalEntries(EntryType entryType) {
        switch (entryType) {
            case user: return entryQueryDAO.getTotalEntries(schema);
            case system: return entryQueryDAO.getTotalSystemEntries(schema);
            default: throw new RuntimeException("Entry type not recognised");
        }
    }
    
    @Override
    public Optional<Instant> getLastUpdatedTime() {
        return entryQueryDAO.getLastUpdatedTime(schema);
    }

    // Item Store

    @Override
    public void addItem(Item item) {
        addItems(Arrays.asList(item));
    }

    @Override
    public Optional<Item> getItemByV1Hash(HashValue hash) {
        return itemQueryDAO.getItemBySHA256(hash.getValue(), schema);
    }

    @Override
    public Optional<Item> getItem(HashValue hash) {
        return itemQueryDAO.getItemByBlobHash(hash.getValue(), schema);
    }

    @Override
    public Collection<Item> getAllItems() {
        return itemQueryDAO.getAllItemsNoPagination(schema);
    }

    @Override
    public Collection<Item> getAllItems(EntryType entryType) {
        return itemQueryDAO.getAllItemsNoPagination(schema, getEntryTable(entryType));
    }

    @Override
    public Collection<Item> getUserItemsPaginated(Optional<HashValue> start, int limit) {
        if(start.isPresent()) {
            return itemQueryDAO.getUserItemsAfter(start.get().getValue(), limit, schema);
        } else {
            return itemQueryDAO.getUserItems(limit, schema);
        }
    }

    @Override
    public Iterator<Item> getItemIterator(EntryType entryType) {
        return itemQueryDAO.getIterator(schema, getEntryTable(entryType));
    }

    @Override
    public Iterator<Item> getItemIterator(int startEntryNumber, int endEntryNumber) {
        return itemQueryDAO.getIterator(startEntryNumber, endEntryNumber, schema);
    }

    // Records

    @Override
    public Optional<Record> getRecord(EntryType entryType, String key) {
        return recordQueryDAO.getRecord(key, schema, getEntryTable(entryType));
    }

    @Override
    public List<Record> getRecords(EntryType entryType, int limit, int offset) {
        return new ArrayList<>(recordQueryDAO.getRecords(limit, offset, schema, getEntryTable(entryType)));
    }

    @Override
    public int getTotalRecords(EntryType entryType) {
        return recordQueryDAO.getTotalRecords(schema, getEntryTable(entryType));
    }

    @Override
    public List<Record> findMax100RecordsByKeyValue(EntryType entryType, String key, String value) {
        return new ArrayList<>(recordQueryDAO.findMax100RecordsByKeyValue(key, value, schema, getEntryTable(entryType)));
    }

    public void addItems(Collection<Item> items) {
        itemDAO.insertInBatch(items, schema);
    }

    public void appendEntries(Collection<Entry> entries) {
        List<Entry> userEntries = entries.stream().filter(entry -> entry.getEntryType() == EntryType.user).collect(Collectors.toList());
        List<Entry> systemEntries = entries.stream().filter(entry -> entry.getEntryType() == EntryType.system).collect(Collectors.toList());

        if (!userEntries.isEmpty()) {
            int currentEntryNumber = getTotalEntries(EntryType.user);
            int expectedNextEntryNumber = currentEntryNumber + 1;

            validateEntryNumbers(userEntries, expectedNextEntryNumber);
            entryDAO.insertInBatch(userEntries, schema, getEntryTable(EntryType.user));

            int maxEntryNumber = currentEntryNumber + userEntries.size();
            entryDAO.setEntryNumber(maxEntryNumber, schema);
        }

        if (!systemEntries.isEmpty()) {
            int currentEntryNumber = getTotalEntries(EntryType.system);
            int expectedNextEntryNumber = currentEntryNumber + 1;

            validateEntryNumbers(systemEntries, expectedNextEntryNumber);
            entryDAO.insertInBatch(systemEntries, schema, getEntryTable(EntryType.system));
        }
    }

    private String getEntryTable(EntryType entryType) {
        switch (entryType) {
            case user: return "entry";
            case system: return "entry_system";
            default: throw new RuntimeException("Entry type not recognised");
        }
    }

    private void validateEntryNumbers(Collection<Entry> entries, int expectedNextEntryNumber) {
        if (!entries.isEmpty()) {
            int nextEntryNumber = entries.stream().min(Comparator.comparingInt(Entry::getEntryNumber)).get().getEntryNumber();
            int maxEntryNumber = nextEntryNumber + entries.size() - 1;

            if (nextEntryNumber != expectedNextEntryNumber
                    || entries.stream().map(Entry::getEntryNumber).distinct().count() != entries.size()
                    || entries.stream().anyMatch(entry -> entry.getEntryNumber() > maxEntryNumber)) {
                throw new IllegalArgumentException("Entry to append skips entry number");
            }
        }
    }
}
