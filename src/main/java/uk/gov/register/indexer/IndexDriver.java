package uk.gov.register.indexer;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Record;
import uk.gov.register.core.Register;
import uk.gov.register.indexer.function.IndexFunction;
import uk.gov.register.store.DataAccessLayer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexDriver {
    private final DataAccessLayer dataAccessLayer;

    public IndexDriver(DataAccessLayer dataAccessLayer) {
        this.dataAccessLayer = dataAccessLayer;
    }

    public void indexEntry(Register register, Entry entry, IndexFunction indexFunction) {
        Optional<Record> currentRecord = register.getDerivationRecord(entry.getKey(), indexFunction.getName());
        Set<IndexKeyItemPair> currentIndexKeyItemPairs = new HashSet<>();
        if (currentRecord.isPresent()) {
            currentIndexKeyItemPairs.addAll(indexFunction.execute(register, currentRecord.get().getEntry()));
        }

        Set<IndexKeyItemPair> newIndexKeyItemPairs = indexFunction.execute(register, entry);

        boolean isRecordIndexFunction = indexFunction.getName().equals("records");
        List<IndexKeyItemPairEvent> pairEvents = getEndIndices(currentIndexKeyItemPairs, newIndexKeyItemPairs, isRecordIndexFunction);
        pairEvents.addAll(getStartIndices(currentIndexKeyItemPairs, newIndexKeyItemPairs, isRecordIndexFunction));

        TreeMap<String, List<IndexKeyItemPairEvent>> sortedEvents = groupEventsByKey(pairEvents);

        AtomicInteger currentIndexEntryNumber = new AtomicInteger(dataAccessLayer.getCurrentIndexEntryNumber(indexFunction.getName()));

        for (Map.Entry<String, List<IndexKeyItemPairEvent>> keyValuePair : sortedEvents.entrySet()) {
            int newIndexEntryNumber = currentIndexEntryNumber.get() + 1;

            for (IndexKeyItemPairEvent p : keyValuePair.getValue()) {
                int existingIndexCountForItem = dataAccessLayer.getExistingIndexCountForItem(indexFunction.getName(), p.getIndexKey(), p.getItemHash().getValue());

                if (p.isStart()) {
                    addIndexKeyToItemHash(indexFunction, currentIndexEntryNumber, entry.getEntryNumber(), newIndexEntryNumber, p, existingIndexCountForItem);
                } else {
                    removeIndexKeyFromItemHash(entry, indexFunction, currentIndexEntryNumber, newIndexEntryNumber, p, existingIndexCountForItem);
                }
            }
        }
    }

    protected List<IndexKeyItemPairEvent> getEndIndices(Set<IndexKeyItemPair> existingPairs, Set<IndexKeyItemPair> newPairs, boolean isRecordIndexFunction) {
        List<IndexKeyItemPairEvent> pairs = new ArrayList<>();

        existingPairs.forEach(existingPair -> {
            if (!newPairs.contains(existingPair) || isRecordIndexFunction) {
                pairs.add(new IndexKeyItemPairEvent(existingPair, false));
            }
        });

        return pairs;
    }

    protected List<IndexKeyItemPairEvent> getStartIndices(Set<IndexKeyItemPair> existingPairs, Set<IndexKeyItemPair> newPairs, boolean isRecordIndexFunction) {
        List<IndexKeyItemPairEvent> pairs = new ArrayList<>();

        newPairs.forEach(newPair -> {
            if (!existingPairs.contains(newPair) || isRecordIndexFunction) {
                pairs.add(new IndexKeyItemPairEvent(newPair, true));
            }
        });

        return pairs;
    }

    private void addIndexKeyToItemHash(IndexFunction indexFunction, AtomicInteger currentIndexEntryNumber, int currentEntryNumber, int newIndexEntryNumber, IndexKeyItemPairEvent p, int existingIndexCountForItem) {
        if (existingIndexCountForItem > 0) {
            dataAccessLayer.start(indexFunction.getName(), p.getIndexKey(), p.getItemHash().getValue(), currentEntryNumber, Optional.empty());
        } else {
            dataAccessLayer.start(indexFunction.getName(), p.getIndexKey(), p.getItemHash().getValue(), currentEntryNumber, Optional.of(newIndexEntryNumber));
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

    private void removeIndexKeyFromItemHash(Entry entry, IndexFunction indexFunction, AtomicInteger currentIndexEntryNumber, int newIndexEntryNumber, IndexKeyItemPairEvent p, int existingIndexCountForItem) {
        if (existingIndexCountForItem > 1) {
            dataAccessLayer.end(indexFunction.getName(), entry.getKey(), p.getIndexKey(), p.getItemHash().getValue(), entry.getEntryNumber(), Optional.empty());
        } else {
            dataAccessLayer.end(indexFunction.getName(), entry.getKey(), p.getIndexKey(), p.getItemHash().getValue(), entry.getEntryNumber(), Optional.of(newIndexEntryNumber));
            currentIndexEntryNumber.set(newIndexEntryNumber);
        }
    }

}
