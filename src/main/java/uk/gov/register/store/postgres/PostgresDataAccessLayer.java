package uk.gov.register.store.postgres;

import com.google.common.collect.Lists;
import uk.gov.register.configuration.IndexFunctionConfiguration.IndexNames;
import uk.gov.register.core.*;
import uk.gov.register.db.*;
import uk.gov.register.exceptions.IndexingException;
import uk.gov.register.indexer.IndexDriver;
import uk.gov.register.indexer.IndexEntryNumberItemCountPair;
import uk.gov.register.indexer.function.IndexFunction;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.util.EntryBlobPair;
import uk.gov.register.util.HashValue;

import java.util.*;
import java.util.stream.Collectors;

public class PostgresDataAccessLayer extends PostgresReadDataAccessLayer implements DataAccessLayer {
    private final List<Entry> stagedEntries;
    private final Map<HashValue, Blob> stagedBlobs;
    private final HashSet<String> stagedEntryKeys;
    private final Map<String, Map<String, List<StartIndex>>> existingStartIndexes;
    private final Map<String, Map<String, List<StartIndex>>> stagedStartIndexes;
    private final Map<String, List<EndIndex>> stagedEndIndexes;

    private final EntryDAO entryDAO;
    private final EntryBlobDAO entryBlobDAO;
    private final BlobDAO blobDAO;
    private final IndexDAO indexDAO;
    private final IndexQueryDAO indexQueryDAO;
    private final IndexDriver indexDriver;
    private final Map<EntryType, Collection<IndexFunction>> indexFunctionsByEntryType;

    public PostgresDataAccessLayer(
            EntryQueryDAO entryQueryDAO, IndexDAO indexDAO, IndexQueryDAO indexQueryDAO, EntryDAO entryDAO,
            EntryBlobDAO entryBlobDAO, BlobQueryDAO blobQueryDAO,
            BlobDAO blobDAO, String schema, IndexDriver indexDriver, Map<EntryType, Collection<IndexFunction>> indexFunctionsByEntryType) {
        super(entryQueryDAO, indexQueryDAO, blobQueryDAO, schema);
        this.entryDAO = entryDAO;
        this.entryBlobDAO = entryBlobDAO;
        this.blobDAO = blobDAO;
        this.indexQueryDAO = indexQueryDAO;
        this.indexDAO = indexDAO;
        
        this.indexDriver = indexDriver;
        this.indexFunctionsByEntryType = indexFunctionsByEntryType;

        stagedEntries = new ArrayList<>();
        stagedBlobs = new HashMap<>();
        stagedEntryKeys = new HashSet<>();
        stagedStartIndexes = new HashMap<>();
        existingStartIndexes = new HashMap<>();
        stagedEndIndexes = new HashMap<>();
    }

    @Override
    public void appendEntry(Entry entry) throws IndexingException {
        stagedEntries.add(entry);
        stagedEntryKeys.add(entry.getKey());

        if (entry.getEntryType().equals(EntryType.system)) {
            checkpoint();
        }
    }

    @Override
    public IndexEntryNumberItemCountPair getStartIndexEntryNumberAndExistingBlobCount(String indexName, String key, String sha256hex){
        if (!existingStartIndexes.containsKey(indexName)) {
            existingStartIndexes.put(indexName, new HashMap<>());
        }

        if (!stagedStartIndexes.containsKey(indexName)) {
            stagedStartIndexes.put(indexName, new HashMap<>());
        }
        
        if (!existingStartIndexes.get(indexName).containsKey(key + sha256hex)) {
            List<StartIndex> currentStartIndexes = indexQueryDAO.getCurrentStartIndexesForKey(indexName, key, sha256hex, schema);
            existingStartIndexes.get(indexName).put(key + sha256hex, currentStartIndexes);
        }
        
        if (existingStartIndexes.get(indexName).get(key + sha256hex).isEmpty() && (!stagedStartIndexes.containsKey(indexName) || stagedStartIndexes.get(indexName).isEmpty() || !stagedStartIndexes.get(indexName).containsKey(key) 
                || stagedStartIndexes.get(indexName).get(key).isEmpty())) {
            return new IndexEntryNumberItemCountPair(Optional.empty(), 0);
        }

        List<StartIndex> currentStartIndexes = new ArrayList<>(existingStartIndexes.get(indexName).get(key + sha256hex));
        
        if (stagedStartIndexes.containsKey(indexName) && stagedStartIndexes.get(indexName).containsKey(key)) {
            currentStartIndexes.addAll(stagedStartIndexes.get(indexName).get(key).stream()
                .filter(i -> i.getItemHash().equals(sha256hex) && !i.isEnded())
                .sorted((i1, i2) -> Integer.compare(i1.getStartIndexEntryNumber(), i2.getStartIndexEntryNumber()))
                .collect(Collectors.toList()));
        }

        if (currentStartIndexes.isEmpty()) {
            return new IndexEntryNumberItemCountPair(Optional.empty(), 0);
        }
        
        return new IndexEntryNumberItemCountPair(Optional.of(currentStartIndexes.get(0).getStartEntryNumber()), currentStartIndexes.size());
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
    public void addBlob(Blob blob) {
        stagedBlobs.put(blob.getSha256hex(), blob);
    }

    @Override
    public void start(String indexName, String key, String blobHash, int startEntryNumber, int startIndexEntryNumber) {
        if (!stagedStartIndexes.containsKey(indexName)) {
            stagedStartIndexes.put(indexName, new HashMap<>());
        }
        
        if (!stagedStartIndexes.get(indexName).containsKey(key)) {
            stagedStartIndexes.get(indexName).put(key, new ArrayList<>());
        }

        stagedStartIndexes.get(indexName).get(key).add(new StartIndex(indexName, key, blobHash, startEntryNumber, startIndexEntryNumber));
    }

    @Override
    public void end(String indexName, String entryKey, String indexKey, String blobHash, int endEntryNumber, int endIndexEntryNumber, int entryNumberToEnd) {
        if (!stagedEndIndexes.containsKey(indexName)) {
            stagedEndIndexes.put(indexName, new ArrayList<>());
        }
        
        stagedEndIndexes.get(indexName).add(new EndIndex(indexName, entryKey, indexKey, blobHash, endEntryNumber, endIndexEntryNumber, entryNumberToEnd));
        
        Optional<StartIndex> currentIndexToEnd = existingStartIndexes.containsKey(indexName) && existingStartIndexes.get(indexName).containsKey(indexKey + blobHash)
                ? existingStartIndexes.get(indexName).get(indexKey + blobHash).stream().filter(i -> i.getItemHash().equals(blobHash) && !i.isEnded()).findFirst()
                : Optional.empty();
        
        if (currentIndexToEnd.isPresent()) {
            currentIndexToEnd.get().end();
            return;
        }

        Optional<StartIndex> stagedIndexToEnd = stagedStartIndexes.containsKey(indexName) && stagedStartIndexes.get(indexName).containsKey(indexKey)
                ? stagedStartIndexes.get(indexName).get(indexKey).stream().filter(i -> i.getItemHash().equals(blobHash) && !i.isEnded()).findFirst()
                : Optional.empty();
        
        if (stagedIndexToEnd.isPresent()) {
            stagedIndexToEnd.get().end();
        }
    }

    @Override
    public Optional<Blob> getBlob(HashValue hash) {
        if (stagedBlobs.containsKey(hash)) {
            return Optional.of(stagedBlobs.get(hash));
        }
        
        return super.getBlob(hash);
    }

    @Override
    public void checkpoint() throws IndexingException {
        updateIndexes();
        
        writeStagedEntriesToDatabase();
        writeStagedItemsToDatabase();

        writeStagedStartIndexesToDatabase();
        writeStagedEndIndexesToDatabase();
    }
    
    private void updateIndexes() throws IndexingException {
        if (stagedEntries.isEmpty()) {
            return;
        }
        
        List<String> keysForStagedEntries = stagedEntryKeys.stream().collect(Collectors.toList());

        for (EntryType entryType : indexFunctionsByEntryType.keySet()) {
            String indexName = entryType == EntryType.user ? IndexNames.RECORD : IndexNames.METADATA;
            Map<String, Entry> entries = getEntriesForKeys(indexName, keysForStagedEntries);

            for (IndexFunction indexFunction : indexFunctionsByEntryType.get(entryType)) {
                int currentIndexEntryNumber = getCurrentIndexEntryNumber(indexFunction.getName());

                // Deep copy the register entries to ensure that each index function gets the same set of entries that were
                // correct for this register as of the time we started updating all indexes. This ensures that index
                // functions can be run in any order, as updating the user or system entries the register first would
                // otherwise alter the outcome of any remaining index function runs.
                Map<String, Entry> tempEntries = new HashMap<>(entries);

                stagedEntries.stream().filter(entry -> entry.getEntryType().equals(entryType)).forEach(entry -> {
                    indexDriver.indexEntry(this, entry, indexFunction, tempEntries, currentIndexEntryNumber);
                });
            }
        }
    }

    private void writeStagedEntriesToDatabase() {
        if (stagedEntries.isEmpty()) {
            return;
        }

        insertEntriesInBatch(EntryType.user, "entry", "entry_item");
        insertEntriesInBatch(EntryType.system, "entry_system", "entry_item_system");
        entryDAO.setEntryNumber(entryDAO.currentEntryNumber(schema) + stagedEntries.stream().filter(e -> e.getEntryType().equals(EntryType.user)).collect(Collectors.toList()).size(), schema);
        stagedEntries.clear();
        stagedEntryKeys.clear();
    }

    private void insertEntriesInBatch(EntryType entryType, String entryTableName, String entryItemTableName) {
        List<Entry> entries = stagedEntries.stream().filter(e -> e.getEntryType().equals(entryType)).collect(Collectors.toList());

        entryDAO.insertInBatch(entries.stream().filter(e -> e.getEntryType().equals(entryType)).collect(Collectors.toList()), schema, entryTableName);
        entryBlobDAO.insertInBatch(entries.stream()
                .filter(e -> e.getEntryType().equals(entryType))
                .flatMap(e -> e.getBlobHashes().stream().map(i -> new EntryBlobPair(e.getEntryNumber(), i)))
                .collect(Collectors.toList()), schema, entryItemTableName);
    }

    private void writeStagedItemsToDatabase() {
        if (stagedBlobs.isEmpty()) {
            return;
        }
        blobDAO.insertInBatch(stagedBlobs.values(), schema);
        stagedBlobs.clear();
    }

    private void writeStagedStartIndexesToDatabase() {
        if (stagedStartIndexes.isEmpty()) {
            return;
        }
        
        List<StartIndex> startIndexes = stagedStartIndexes.values().stream().flatMap(m -> m.values().stream().flatMap(l -> l.stream())).collect(Collectors.toList());
        indexDAO.startInBatch(startIndexes, schema);
        
        stagedStartIndexes.clear();
        existingStartIndexes.clear();
    }

    private void writeStagedEndIndexesToDatabase() {
        if (stagedEndIndexes.isEmpty()) {
            return;
        }

        stagedEndIndexes.keySet().forEach(indexName -> {
            String entryTable = indexName.equals(IndexNames.METADATA) ? "entry_system" : "entry";
            List<EndIndex> endIndexes = stagedEndIndexes.get(indexName);
            indexDAO.endInBatch(endIndexes, schema, entryTable);
        });

        stagedEndIndexes.clear();
    }
    
    private OptionalInt getMaxStagedEntryNumber() {
        if (stagedEntries.isEmpty()) {
            return OptionalInt.empty();
        }
        
        return OptionalInt.of(stagedEntries.get(stagedEntries.size() - 1).getEntryNumber());
    }
    
    private Map<String, Record> getIndexRecordsForKeys(String indexName, List<String> entryKeys) {
        Map<String, Record> indexRecords = new HashMap<>();
        
        if (getTotalRecords(indexName) > 0) {
            List<List<String>> entryKeyBatches = Lists.partition(entryKeys, 1000);
            entryKeyBatches.stream().forEach(keyBatches -> {
                indexRecords.putAll(getIndexRecords(keyBatches, indexName).stream().collect(Collectors.toMap(k -> k.getEntry().getKey(), v -> v)));
            });
        }
        
        return indexRecords;
    }
    
    private Map<String, Entry> getEntriesForKeys(String indexName, List<String> entryKeys) {
        int totalEntries = indexName.equals(IndexNames.METADATA) ? entryQueryDAO.getTotalSystemEntries(schema) : entryQueryDAO.getTotalEntries(schema);
        
        if (totalEntries == 0) {
            return new HashMap<>();
        }
        
        List<Entry> entries = new ArrayList<>();
            
        String entryTable = indexName.equals(IndexNames.METADATA) ? "entry_system" : "entry";
        String entryItemTable = indexName.equals(IndexNames.METADATA) ? "entry_item_system" : "entry_item";

        List<List<String>> entryKeyBatches = Lists.partition(entryKeys, 1000);
        entryKeyBatches.stream().forEach(keyBatches -> {
            entries.addAll(entryQueryDAO.getEntriesByKeys(keyBatches, schema, entryTable, entryItemTable));
        });

        return entries.stream().collect(Collectors.toMap(k -> k.getKey(), e -> e, (e1, e2) -> e2));
    }
}
