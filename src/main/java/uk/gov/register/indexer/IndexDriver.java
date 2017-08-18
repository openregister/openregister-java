package uk.gov.register.indexer;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.indexer.function.IndexFunction;
import uk.gov.register.store.DataAccessLayer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class IndexDriver {
    private final Map<String, AtomicInteger> currentIndexEntryNumbers;

    public IndexDriver() {
        this.currentIndexEntryNumbers = new HashMap<>();
    }

    public void indexEntry(DataAccessLayer dataAccessLayer, Entry entry, IndexFunction indexFunction,
                           Map<String, Record> indexRecords, final int initialIndexEntryNumber) {
        if (!currentIndexEntryNumbers.containsKey(indexFunction.getName())) {
            currentIndexEntryNumbers.put(indexFunction.getName(), new AtomicInteger(initialIndexEntryNumber));
        }

        Optional<Record> currentRecord = indexRecords.containsKey(entry.getKey())
                ? Optional.of(indexRecords.get(entry.getKey()))
                : Optional.empty();

        // Always update our cached index records to reflect the incoming entry
        List<Item> items = entry.getItemHashes().stream().map(h -> dataAccessLayer.getItemBySha256(h).get()).collect(Collectors.toList());
        indexRecords.put(entry.getKey(), new Record(entry, items));

        Set<IndexKeyItemPair> currentIndexKeyItemPairs = new HashSet<>();
        if (currentRecord.isPresent()) {
            currentIndexKeyItemPairs.addAll(indexFunction.execute(dataAccessLayer::getItemBySha256, currentRecord.get().getEntry()));
        }

        Set<IndexKeyItemPair> newIndexKeyItemPairs = indexFunction.execute(dataAccessLayer::getItemBySha256, entry);

        List<IndexKeyItemPairEvent> pairEvents = getEndIndices(currentIndexKeyItemPairs, newIndexKeyItemPairs);
        pairEvents.addAll(getStartIndices(currentIndexKeyItemPairs, newIndexKeyItemPairs));

        TreeMap<String, List<IndexKeyItemPairEvent>> sortedEvents = groupEventsByKey(pairEvents);

        for (Map.Entry<String, List<IndexKeyItemPairEvent>> keyValuePair : sortedEvents.entrySet()) {
            AtomicInteger currentIndexEntryNumber = currentIndexEntryNumbers.get(indexFunction.getName());
            int newIndexEntryNumber = currentIndexEntryNumber.get() + 1;

            for (IndexKeyItemPairEvent p : keyValuePair.getValue()) {
                IndexEntryNumberItemCountPair startIndexEntryNumberItemCountPair = dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount(indexFunction.getName(), p.getIndexKey(), p.getItemHash().getValue());

                if (p.isStart()) {
                    addIndexKeyToItemHash(indexFunction, dataAccessLayer, currentIndexEntryNumber, entry.getEntryNumber(), newIndexEntryNumber, p, startIndexEntryNumberItemCountPair);
                } else {
                    removeIndexKeyFromItemHash(entry, indexFunction, dataAccessLayer, currentIndexEntryNumber, newIndexEntryNumber, p, startIndexEntryNumberItemCountPair, currentRecord.get().getEntry().getEntryNumber());
                }
            }
        }
    }

    protected List<IndexKeyItemPairEvent> getEndIndices(Set<IndexKeyItemPair> existingPairs, Set<IndexKeyItemPair> newPairs) {
        List<IndexKeyItemPairEvent> pairs = new ArrayList<>();

        existingPairs.forEach(existingPair -> {
            if (!newPairs.contains(existingPair)) {
                pairs.add(new IndexKeyItemPairEvent(existingPair, false));
            }
        });

        return pairs;
    }

    protected List<IndexKeyItemPairEvent> getStartIndices(Set<IndexKeyItemPair> existingPairs, Set<IndexKeyItemPair> newPairs) {
        List<IndexKeyItemPairEvent> pairs = new ArrayList<>();

        newPairs.forEach(newPair -> {
            if (!existingPairs.contains(newPair)) {
                pairs.add(new IndexKeyItemPairEvent(newPair, true));
            }
        });

        return pairs;
    }

    private void addIndexKeyToItemHash(IndexFunction indexFunction, DataAccessLayer dataAccessLayer, AtomicInteger currentIndexEntryNumber, int currentEntryNumber, int newIndexEntryNumber, IndexKeyItemPairEvent p, IndexEntryNumberItemCountPair startIndexEntryNumberItemCountPair) {
        if (startIndexEntryNumberItemCountPair.getStartIndexEntryNumber().isPresent()) {
            dataAccessLayer.start(indexFunction.getName(), p.getIndexKey(), p.getItemHash().getValue(), currentEntryNumber, startIndexEntryNumberItemCountPair.getStartIndexEntryNumber().get());
        } else {
            dataAccessLayer.start(indexFunction.getName(), p.getIndexKey(), p.getItemHash().getValue(), currentEntryNumber, newIndexEntryNumber);
            currentIndexEntryNumber.set(newIndexEntryNumber);
        }
    }

    private TreeMap<String, List<IndexKeyItemPairEvent>> groupEventsByKey(List<IndexKeyItemPairEvent> pairEvents) {
        TreeMap<String, List<IndexKeyItemPairEvent>> sortedEvents = new TreeMap<>();
        pairEvents.forEach(e -> {
            if (!sortedEvents.containsKey(e.getIndexKey())) {
                sortedEvents.put(e.getIndexKey(), new ArrayList<>());
            }

            sortedEvents.get(e.getIndexKey()).add(e);
        });

        return sortedEvents;
    }

    private void removeIndexKeyFromItemHash(Entry entry, IndexFunction indexFunction, DataAccessLayer dataAccessLayer, AtomicInteger currentIndexEntryNumber, int newIndexEntryNumber, IndexKeyItemPairEvent p, IndexEntryNumberItemCountPair startIndexEntryNumberItemCountPair, int entryNumberToEnd) {
        if (startIndexEntryNumberItemCountPair.getExistingItemCount() > 1) {
            dataAccessLayer.end(indexFunction.getName(), entry.getKey(), p.getIndexKey(), p.getItemHash().getValue(), entry.getEntryNumber(), startIndexEntryNumberItemCountPair.getStartIndexEntryNumber().get(), entryNumberToEnd);
        } else {
            dataAccessLayer.end(indexFunction.getName(), entry.getKey(), p.getIndexKey(), p.getItemHash().getValue(), entry.getEntryNumber(), newIndexEntryNumber, entryNumberToEnd);
            currentIndexEntryNumber.set(newIndexEntryNumber);
        }
    }
}
