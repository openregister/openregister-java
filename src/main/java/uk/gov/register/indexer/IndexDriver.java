package uk.gov.register.indexer;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Record;
import uk.gov.register.core.Register;
import uk.gov.register.db.IndexDAO;
import uk.gov.register.db.IndexQueryDAO;
import uk.gov.register.indexer.function.IndexFunction;

import java.util.*;

public class IndexDriver {
    private final Register register;
    private final IndexDAO indexDAO;
    private final IndexQueryDAO indexQueryDAO;

    public IndexDriver(Register register, IndexDAO indexDAO, IndexQueryDAO indexQueryDAO) {
        this.indexDAO = indexDAO;
        this.register = register;
        this.indexQueryDAO = indexQueryDAO;
    }

    public void indexEntry(Entry entry, IndexFunction indexFunction) {
        register.commit();
        Optional<Record> currentRecord = register.getRecord(entry.getKey());
        Set<IndexValueItemPair> currentIndexValueItemPairs = new HashSet<>();
        if (currentRecord.isPresent()) {
            currentIndexValueItemPairs.addAll(indexFunction.execute(currentRecord.get().getEntry()));
        }

        Set<IndexValueItemPair> newIndexValueItemPairs = indexFunction.execute(entry);

        List<IndexValueItemPairEvent> pairEvents = getEndIndices(currentIndexValueItemPairs, newIndexValueItemPairs);
        pairEvents.addAll(getStartIndices(currentIndexValueItemPairs, newIndexValueItemPairs));

        TreeMap<String, List<IndexValueItemPairEvent>> sortedEvents = new TreeMap<>();
        pairEvents.forEach(e -> {
            if (!sortedEvents.containsKey(e.getIndexValue())) {
                sortedEvents.put(e.getIndexValue(), new ArrayList<>());
            }

            sortedEvents.get(e.getIndexValue()).add(e);
        });

        int currentIndexEntryNumber = indexQueryDAO.getCurrentIndexEntryNumber(indexFunction.getName());
        int currentEntryNumber = entry.getEntryNumber();

        for (Map.Entry<String, List<IndexValueItemPairEvent>> keyValuePair : sortedEvents.entrySet()) {
            currentIndexEntryNumber++;

            for (IndexValueItemPairEvent p : keyValuePair.getValue()) {
                if (p.isStart()) {
                    indexDAO.start(indexFunction.getName(), p.getIndexValue(), p.getItemHash().getValue(), currentEntryNumber, currentIndexEntryNumber);
                }
                else {
                    indexDAO.end(indexFunction.getName(), p.getIndexValue(), p.getItemHash().getValue(), currentEntryNumber, currentIndexEntryNumber);
                }
            }
        }
    }

    protected List<IndexValueItemPairEvent> getEndIndices(Set<IndexValueItemPair> existingPairs, Set<IndexValueItemPair> newPairs) {
        List<IndexValueItemPairEvent> pairs = new ArrayList<>();

        existingPairs.forEach(existingPair -> {
            if (!newPairs.contains(existingPair)) {
                pairs.add(new IndexValueItemPairEvent(existingPair, false));
            }
        });

        return pairs;
    }

    protected List<IndexValueItemPairEvent> getStartIndices(Set<IndexValueItemPair> existingPairs, Set<IndexValueItemPair> newPairs) {
        List<IndexValueItemPairEvent> pairs = new ArrayList<>();

        newPairs.forEach(newPair -> {
            if (!existingPairs.contains(newPair)) {
                pairs.add(new IndexValueItemPairEvent(newPair, true));
            }
        });

        return pairs;
    }
}
