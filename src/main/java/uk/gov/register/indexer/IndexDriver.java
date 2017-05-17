package uk.gov.register.indexer;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Record;
import uk.gov.register.core.Register;
import uk.gov.register.db.IndexDAO;
import uk.gov.register.db.IndexQueryDAO;
import uk.gov.register.indexer.function.IndexFunction;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexDriver {
    private final Register register;
    private final IndexDAO indexDAO;
    private final IndexQueryDAO indexQueryDAO;

    public IndexDriver(Register register, IndexDAO indexDAO, IndexQueryDAO indexQueryDAO) {
        this.indexDAO = indexDAO;
        this.register = register;
        this.indexQueryDAO = indexQueryDAO;
    }

    public void indexEntry(Register register, Entry entry, IndexFunction indexFunction) {
        Optional<Record> currentRecord = register.getRecord(entry.getKey());
        Set<IndexKeyItemPair> currentIndexKeyItemPairs = new HashSet<>();
        if (currentRecord.isPresent()) {
            currentIndexKeyItemPairs.addAll(indexFunction.execute(register, currentRecord.get().getEntry()));
        }

        Set<IndexKeyItemPair> newIndexKeyItemPairs = indexFunction.execute(register, entry);

        List<IndexKeyItemPairEvent> pairEvents = getEndIndices(currentIndexKeyItemPairs, newIndexKeyItemPairs);
        pairEvents.addAll(getStartIndices(currentIndexKeyItemPairs, newIndexKeyItemPairs));

        TreeMap<String, List<IndexKeyItemPairEvent>> sortedEvents = groupEventsByKey(pairEvents);

        AtomicInteger currentIndexEntryNumber = new AtomicInteger(indexQueryDAO.getCurrentIndexEntryNumber(indexFunction.getName()));

        for (Map.Entry<String, List<IndexKeyItemPairEvent>> keyValuePair : sortedEvents.entrySet()) {
            int newIndexEntryNumber = currentIndexEntryNumber.get() + 1;

            for (IndexKeyItemPairEvent p : keyValuePair.getValue()) {
                int existingIndexCountForItem = indexQueryDAO.getExistingIndexCountForItem(indexFunction.getName(), p.getIndexKey(), p.getItemHash().getValue());

                if (p.isStart()) {
                    addIndexKeyToItemHash(indexFunction, currentIndexEntryNumber, entry.getEntryNumber(), newIndexEntryNumber, p, existingIndexCountForItem);
                }
                else {
                    removeIndexKeyFromItemHash(entry, indexFunction, currentIndexEntryNumber, newIndexEntryNumber, p, existingIndexCountForItem);
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

    private void addIndexKeyToItemHash(IndexFunction indexFunction, AtomicInteger currentIndexEntryNumber, int currentEntryNumber, int newIndexEntryNumber, IndexKeyItemPairEvent p, int existingIndexCountForItem) {
        if (existingIndexCountForItem > 0) {
            indexDAO.start(indexFunction.getName(), p.getIndexKey(), p.getItemHash().getValue(), currentEntryNumber, Optional.empty());
        }
        else {
            indexDAO.start(indexFunction.getName(), p.getIndexKey(), p.getItemHash().getValue(), currentEntryNumber, Optional.of(newIndexEntryNumber));
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
            indexDAO.end(indexFunction.getName(), entry.getKey(), p.getIndexKey(), p.getItemHash().getValue(), entry.getEntryNumber(), Optional.empty());
        } else {
            indexDAO.end(indexFunction.getName(), entry.getKey(), p.getIndexKey(), p.getItemHash().getValue(), entry.getEntryNumber(), Optional.of(newIndexEntryNumber));
            currentIndexEntryNumber.set(newIndexEntryNumber);
        }
    }

}
