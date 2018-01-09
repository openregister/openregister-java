package uk.gov.register.indexer;

import org.apache.commons.collections4.CollectionUtils;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.exceptions.EntryValidationException;
import uk.gov.register.indexer.function.IndexFunction;
import uk.gov.register.store.DataAccessLayer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexDriver {
    private final Map<String, AtomicInteger> currentIndexEntryNumbers;

    public IndexDriver() {
        this.currentIndexEntryNumbers = new HashMap<>();
    }

    public void indexEntry(DataAccessLayer dataAccessLayer, Entry entry, IndexFunction indexFunction,
        Map<String, Entry> entries, final int initialIndexEntryNumber) {
        if (!currentIndexEntryNumbers.containsKey(indexFunction.getName())) {
            currentIndexEntryNumbers.put(indexFunction.getName(), new AtomicInteger(initialIndexEntryNumber));
        }

        Optional<Entry> currentEntry = entries.containsKey(entry.getKey())
                ? Optional.of(entries.get(entry.getKey()))
                : Optional.empty();

        if (entry.getEntryType() == EntryType.user) {
            if (currentEntry.isPresent() && currentEntry.get().getItemHashes().isEmpty() && entry.getItemHashes().isEmpty()) {
                throw new EntryValidationException(entry, "Cannot tombstone a record which does not exist");
            }
            else if (currentEntry.isPresent() && CollectionUtils.isEqualCollection(currentEntry.get().getItemHashes(), entry.getItemHashes())) {
                throw new EntryValidationException(entry, "Cannot contain identical items to previous entry");
            }
        }

        // Always update our cached entries to reflect the incoming entry
        entries.put(entry.getKey(), entry);

        Set<IndexKeyItemPair> currentIndexKeyItemPairs = new HashSet<>();
        currentEntry.ifPresent(e -> currentIndexKeyItemPairs.addAll(indexFunction.execute(dataAccessLayer::getItemBySha256, e)));

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
                    removeIndexKeyFromItemHash(entry, indexFunction, dataAccessLayer, currentIndexEntryNumber, newIndexEntryNumber, p, startIndexEntryNumberItemCountPair, currentEntry.get().getEntryNumber());
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
