package uk.gov.register.indexer;

import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
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
        Optional<Record> currentRecord = (entry.getEntryType() == EntryType.user)
                ? register.getRecord(entry.getKey())
                : register.getDerivationRecord(entry.getKey(), "metadata");
        Set<IndexKeyItemPair> currentIndexKeyItemPairs = new HashSet<>();
        if (currentRecord.isPresent()) {
            currentIndexKeyItemPairs.addAll(indexFunction.execute(register, currentRecord.get().getEntry()));
        }

        Set<IndexKeyItemPair> newIndexKeyItemPairs = indexFunction.execute(register, entry);

        List<IndexKeyItemPairEvent> pairEvents = getEndIndices(currentIndexKeyItemPairs, newIndexKeyItemPairs);
        pairEvents.addAll(getStartIndices(currentIndexKeyItemPairs, newIndexKeyItemPairs));

        TreeMap<String, List<IndexKeyItemPairEvent>> sortedEvents = groupEventsByKey(pairEvents);

        AtomicInteger currentIndexEntryNumber = new AtomicInteger(dataAccessLayer.getCurrentIndexEntryNumber(indexFunction.getName()));

        for (Map.Entry<String, List<IndexKeyItemPairEvent>> keyValuePair : sortedEvents.entrySet()) {
            int newIndexEntryNumber = currentIndexEntryNumber.get() + 1;

            for (IndexKeyItemPairEvent p : keyValuePair.getValue()) {
                IndexEntryNumberItemCountPair startIndexEntryNumberItemCountPair = dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount(indexFunction.getName(), p.getIndexKey(), p.getItemHash().getValue());

                if (p.isStart()) {
                    addIndexKeyToItemHash(indexFunction, currentIndexEntryNumber, entry.getEntryNumber(), newIndexEntryNumber, p, startIndexEntryNumberItemCountPair);
                } else {
                    removeIndexKeyFromItemHash(entry, indexFunction, currentIndexEntryNumber, newIndexEntryNumber, p, startIndexEntryNumberItemCountPair);
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

    private void addIndexKeyToItemHash(IndexFunction indexFunction, AtomicInteger currentIndexEntryNumber, int currentEntryNumber, int newIndexEntryNumber, IndexKeyItemPairEvent p, IndexEntryNumberItemCountPair startIndexEntryNumberItemCountPair) {
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

    private void removeIndexKeyFromItemHash(Entry entry, IndexFunction indexFunction, AtomicInteger currentIndexEntryNumber, int newIndexEntryNumber, IndexKeyItemPairEvent p, IndexEntryNumberItemCountPair startIndexEntryNumberItemCountPair) {
        if (startIndexEntryNumberItemCountPair.getExistingItemCount() > 1) {
            dataAccessLayer.end(indexFunction.getName(), entry.getKey(), p.getIndexKey(), p.getItemHash().getValue(), entry.getEntryNumber(), startIndexEntryNumberItemCountPair.getStartIndexEntryNumber().get());
        } else {
            dataAccessLayer.end(indexFunction.getName(), entry.getKey(), p.getIndexKey(), p.getItemHash().getValue(), entry.getEntryNumber(), newIndexEntryNumber);
            currentIndexEntryNumber.set(newIndexEntryNumber);
        }
    }

}
