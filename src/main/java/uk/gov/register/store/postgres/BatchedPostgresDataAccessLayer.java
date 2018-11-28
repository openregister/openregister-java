package uk.gov.register.store.postgres;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.register.core.*;
import uk.gov.register.db.*;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BatchedPostgresDataAccessLayer implements DataAccessLayer {
    private final Map<EntryType, TreeMap<Integer, Entry>> batchedEntries;
    private final Map<EntryType, Map<String, Integer>> batchedRecords;
    private final Map<HashValue, Item> batchedItems;

    private final PostgresDataAccessLayer postgresDataAccessLayer;

    private boolean isEntireRegisterBatched;

    public BatchedPostgresDataAccessLayer(PostgresDataAccessLayer postgresDataAccessLayer) {
        this.postgresDataAccessLayer = postgresDataAccessLayer;

        batchedEntries = new HashMap<>();
        batchedItems = new HashMap<>();
        batchedRecords = new HashMap<>();

        isEntireRegisterBatched = false;
    }

    //Entry log

    @Override
    public void appendEntry(Entry entry) {
        if (entry.getEntryNumber() != getTotalEntries(entry.getEntryType()) + 1) {
            throw new IllegalArgumentException("Entry to append skips entry number");
        }

        if (!batchedEntries.containsKey(entry.getEntryType())) {
            batchedEntries.put(entry.getEntryType(), new TreeMap<>());
        }

        if (!batchedRecords.containsKey(entry.getEntryType())) {
            batchedRecords.put(entry.getEntryType(), new HashMap<>());
        }

        batchedEntries.get(entry.getEntryType()).put(entry.getEntryNumber(), entry);
        batchedRecords.get(entry.getEntryType()).put(entry.getKey(), entry.getEntryNumber());

        if (entry.getEntryType() == EntryType.system && entry.getEntryNumber() == 1) {
            isEntireRegisterBatched = true;
        }
    }

    @Override
    public Optional<Entry> getEntry(int entryNumber) {
        if (batchedEntries.containsKey(EntryType.user)) {
            TreeMap<Integer, Entry> entries = batchedEntries.get(EntryType.user);
            if (entries.containsKey(entryNumber)) {
                return Optional.of(entries.get(entryNumber));
            }
        }

        return isEntireRegisterBatched ? Optional.empty() : postgresDataAccessLayer.getEntry(entryNumber);
    }

    @Override
    public Collection<Entry> getEntries(int start, int limit) {
        if (batchedEntries.containsKey(EntryType.user)) {
            TreeMap<Integer, Entry> entries = batchedEntries.get(EntryType.user);

            if (entries.containsKey(start) && entries.containsKey(start + limit - 1)) {
                return entries.values().stream()
                        .filter(entry -> entry.getEntryNumber() >= start && entry.getEntryNumber() < start + 100)
                        .sorted(Comparator.comparing(Entry::getEntryNumber))
                        .collect(Collectors.toList());
            }
        }

        writeBatchesToDatabase();
        return postgresDataAccessLayer.getEntries(start, limit);
    }

    @Override
    public Collection<Entry> getAllEntries() {
        if (isEntireRegisterBatched && batchedEntries.containsKey(EntryType.user)) {
            return batchedEntries.get(EntryType.user).descendingMap().values();
        }

        writeBatchesToDatabase();
        return postgresDataAccessLayer.getAllEntries();
    }

    @Override
    public Collection<Entry> getAllEntriesByKey(String key) {
        if (isEntireRegisterBatched && batchedEntries.containsKey(EntryType.user)) {
            return batchedEntries.get(EntryType.user).values().stream()
                    .filter(entry -> entry.getKey().equals(key))
                    .collect(Collectors.toList());
        }

        writeBatchesToDatabase();
        return postgresDataAccessLayer.getAllEntriesByKey(key);
    }

    @Override
    public Iterator<Entry> getEntryIterator(EntryType entryType) {
        if (isEntireRegisterBatched && batchedEntries.containsKey(entryType)) {
            return batchedEntries.get(entryType).values().stream().iterator();
        }

        writeBatchesToDatabase();
        return postgresDataAccessLayer.getEntryIterator(entryType);
    }

    @Override
    public Iterator<Entry> getEntryIterator(EntryType entryType, int totalEntries1, int totalEntries2) {
        if (batchedEntries.containsKey(entryType)) {
            TreeMap<Integer, Entry> entries = batchedEntries.get(entryType);

            if (entries.containsKey(totalEntries1 + 1) && entries.containsKey(totalEntries2)) {
                return entries.values().stream()
                        .filter(entry -> entry.getEntryNumber() > totalEntries1 && entry.getEntryNumber() <= totalEntries2)
                        .sorted(Comparator.comparing(Entry::getEntryNumber))
                        .iterator();
            }
        }

        writeBatchesToDatabase();
        return postgresDataAccessLayer.getEntryIterator(entryType, totalEntries1, totalEntries2);
    }

    @Override
    public <R> R withEntryIterator(Function<EntryIterator, R> callback) {
        writeBatchesToDatabase();
        return postgresDataAccessLayer.withEntryIterator(callback);
    }

    @Override
    public int getTotalEntries(EntryType entryType) {
        if (batchedEntries.containsKey(entryType)) {
            return batchedEntries.get(entryType).lastKey();
        }

        return postgresDataAccessLayer.getTotalEntries(entryType);
    }

    @Override
    public Optional<Instant> getLastUpdatedTime() {
        if (batchedEntries.containsKey(EntryType.user)) {
            TreeMap<Integer, Entry> entries = batchedEntries.get(EntryType.user);

            if (!entries.isEmpty()) {
                return Optional.of(batchedEntries.get(EntryType.user).lastEntry().getValue().getTimestamp());
            }
        }

        return isEntireRegisterBatched ? Optional.empty() : postgresDataAccessLayer.getLastUpdatedTime();
    }

    // Item store

    @Override
    public void addItem(Item item) {
        batchedItems.put(item.getSha256hex(), item);
    }

    @Override
    public Optional<Item> getItemByV1Hash(HashValue hash) {
        if (batchedItems.containsKey(hash)) {
            return Optional.of(batchedItems.get(hash));
        }
        
        return isEntireRegisterBatched ? Optional.empty() : postgresDataAccessLayer.getItemByV1Hash(hash);
    }

    @Override
    public Optional<Item> getItem(HashValue hash) {
        // TODO: this layer uses old hashes to store stuff in memory, so this lookup
        // can't be cached. If this is caching is still necessary, migrate this to use
        // new hashes.

        return isEntireRegisterBatched ? Optional.empty() : postgresDataAccessLayer.getItem(hash);
    }

    @Override
    public Collection<Item> getAllItems() {
        if (isEntireRegisterBatched) {
            return batchedItems.values();
        }

        writeBatchesToDatabase();
        return postgresDataAccessLayer.getAllItems();
    }

    @Override
    public Collection<Item> getAllItems(EntryType entryType) {
        return postgresDataAccessLayer.getAllItems(entryType);
    }

    @Override
    public Iterator<Item> getItemIterator(EntryType entryType) {
        // It's possible for an entry to be loaded where the item already exists in the database and is therefore not batched (as per RSF rules).
        // Therefore this statement only entered if entire register is batched.
        if (isEntireRegisterBatched && batchedEntries.containsKey(entryType)) {
            List<HashValue> itemHashes = batchedEntries.get(entryType).values().stream().map(Entry::getItemHash).collect(Collectors.toList());
            return batchedItems.entrySet().stream().filter(item -> itemHashes.contains(item.getKey())).map(Map.Entry::getValue).iterator();
        }

        writeBatchesToDatabase();
        return postgresDataAccessLayer.getItemIterator(entryType);
    }

    @Override
    public Iterator<Item> getItemIterator(int startEntryNumber, int endEntryNumber) {
        // It's possible for an entry to be loaded where the item already exists in the database and is therefore not batched (as per RSF rules).
        // Therefore this statement only entered if entire register is batched.
        if (isEntireRegisterBatched && batchedEntries.containsKey(EntryType.user)) {
            TreeMap<Integer, Entry> entries = batchedEntries.get(EntryType.user);

            if (entries.containsKey(startEntryNumber + 1) && entries.containsKey(endEntryNumber)) {
                List<HashValue> itemHashes = entries.values().stream().filter(entry -> entry.getEntryNumber() > startEntryNumber && entry.getEntryNumber() <= endEntryNumber).map(Entry::getItemHash).collect(Collectors.toList());
                return batchedItems.entrySet().stream().filter(item -> itemHashes.contains(item.getKey())).map(Map.Entry::getValue).iterator();
            }
        }

        writeBatchesToDatabase();
        return postgresDataAccessLayer.getItemIterator(startEntryNumber, endEntryNumber);
    }

    // Records

    @Override
    public Optional<Record> getRecord(EntryType entryType, String key) {
        if (batchedRecords.containsKey(entryType)) {
            Map<String, Integer> recordEntryNumbers = batchedRecords.get(entryType);

            if (recordEntryNumbers.containsKey(key)) {
                try {
                    Entry entry = batchedEntries.get(entryType).get(recordEntryNumbers.get(key));

                    // It's possible for an entry to be loaded where the item already exists in the database and is therefore not batched (as per RSF rules).
                    // Therefore use existing getItemByV1Hash(HashValue hash) logic which goes to the database if necessary.
                    Item item = getItemByV1Hash(entry.getItemHash()).get();

                    return Optional.of(new Record(entry, item));
                } catch (NullPointerException | NoSuchElementException ex) {
                    // This is unexpected but will occur if batchedEntries is somehow inconsistent with batchedRecords or if the item does not exist
                    throw new RuntimeException("Unexpected error in getting record from batched data", ex);
                }
            }
        }

        return isEntireRegisterBatched ? Optional.empty() : postgresDataAccessLayer.getRecord(entryType, key);
    }

    @Override
    public List<Record> getRecords(EntryType entryType, int limit, int offset) {
        if (isEntireRegisterBatched && batchedRecords.containsKey(entryType)) {
            Map<String, Integer> recordEntryNumbers = batchedRecords.get(entryType);

            if (!recordEntryNumbers.isEmpty()) {
                    return recordEntryNumbers.values().stream().map(entryNumber -> {
                        try {
                            Entry entry = batchedEntries.get(entryType).get(entryNumber);

                            // It's possible for an entry to be loaded where the item already exists in the database and is therefore not batched (as per RSF rules).
                            // Therefore use existing getItemByV1Hash(HashValue hash) logic which goes to the database if necessary.
                            Item item = getItemByV1Hash(entry.getItemHash()).get();

                            return new Record(entry, item);
                        } catch (NullPointerException | NoSuchElementException ex) {
                            // This is unexpected but will occur if batchedEntries is somehow inconsistent with batchedRecords or if the item does not exist
                            throw new RuntimeException("Unexpected error in getting record from batched data", ex);
                        }
                    }).sorted((record1, record2) -> Integer.compare(record2.getEntry().getEntryNumber(), record1.getEntry().getEntryNumber()))
                            .skip(offset)
                            .limit(limit)
                            .collect(Collectors.toList());
            }
        }

        writeBatchesToDatabase();
        return postgresDataAccessLayer.getRecords(entryType, limit, offset);
    }

    @Override
    public List<Record> findMax100RecordsByKeyValue(EntryType entryType, String key, String value) {
        if (isEntireRegisterBatched && batchedRecords.containsKey(entryType)) {
            Map<String, Integer> recordEntryNumbers = batchedRecords.get(entryType);

            if (!recordEntryNumbers.isEmpty()) {
                return recordEntryNumbers.values().stream().map(entryNumber -> {
                    try {
                        Entry entry = batchedEntries.get(entryType).get(entryNumber);

                        // It's possible for an entry to be loaded where the item already exists in the database and is therefore not batched (as per RSF rules).
                        // Therefore use existing getItemByV1Hash(HashValue hash) logic which goes to the database if necessary.
                        Item item = getItemByV1Hash(entry.getItemHash()).get();

                        return new Record(entry, item);
                    } catch (NullPointerException | NoSuchElementException ex) {
                        // This is unexpected but will occur if batchedEntries is somehow inconsistent with batchedRecords or if the item does not exist
                        throw new RuntimeException("Unexpected error in getting record from batched data", ex);
                    }
                }).filter(record ->  {
                    JsonNode node = record.getItem().getContent().get(key);

                    return node != null && node.isTextual() && node.asText().equals(value);
                }).sorted((record1, record2) -> Integer.compare(record2.getEntry().getEntryNumber(), record1.getEntry().getEntryNumber()))
                        .limit(100)
                        .collect(Collectors.toList());
            }
        }

        writeBatchesToDatabase();
        return postgresDataAccessLayer.findMax100RecordsByKeyValue(entryType, key, value);
    }

    @Override
    public int getTotalRecords(EntryType entryType) {
        if (isEntireRegisterBatched && batchedRecords.containsKey(entryType)) {
            return batchedRecords.get(entryType).size();
        }

        writeBatchesToDatabase();
        return postgresDataAccessLayer.getTotalRecords(entryType);
    }

    public void writeBatchesToDatabase() {
        writeStagedEntriesToDatabase();
        writeStagedItemsToDatabase();
    }

    private void writeStagedEntriesToDatabase() {
        if (batchedEntries.isEmpty()) {
            return;
        }

        if (batchedEntries.containsKey(EntryType.user)) {
            postgresDataAccessLayer.appendEntries(batchedEntries.get(EntryType.user).values());
        }

        if (batchedEntries.containsKey(EntryType.system)) {
            postgresDataAccessLayer.appendEntries(batchedEntries.get(EntryType.system).values());
        }

        batchedEntries.clear();
        batchedRecords.clear();
        isEntireRegisterBatched = false;
    }

    private void writeStagedItemsToDatabase() {
        if (batchedItems.isEmpty()) {
            return;
        }

        postgresDataAccessLayer.addItems(batchedItems.values());

        batchedItems.clear();
        isEntireRegisterBatched = false;
    }
}
