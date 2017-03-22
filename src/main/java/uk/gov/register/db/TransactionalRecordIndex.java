package uk.gov.register.db;

import com.google.common.collect.Iterables;
import uk.gov.register.core.Entry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

public class TransactionalRecordIndex extends AbstractRecordIndex {
    private final HashMap<String, Integer> stagedCurrentKeys;
    private final Set<String> entriesWithoutItems;
    private final CurrentKeysUpdateDAO currentKeysDAO;

    public TransactionalRecordIndex(RecordQueryDAO recordQueryDAO, CurrentKeysUpdateDAO currentKeysDAO) {
        super(recordQueryDAO);
        this.currentKeysDAO = currentKeysDAO;
        stagedCurrentKeys = new HashMap<>();
        entriesWithoutItems = new HashSet<>();
    }

    @Override
    public void updateRecordIndex(Entry entry) {
        stagedCurrentKeys.put(entry.getKey(), entry.getEntryNumber());

        if (entry.getItemHashes().isEmpty()) {
            entriesWithoutItems.add(entry.getKey());
        }
    }

    @Override
    public void checkpoint() {
        int noOfRecordsDeleted = removeRecordsWithKeys(stagedCurrentKeys.keySet());

        currentKeysDAO.writeCurrentKeys(Iterables.transform(stagedCurrentKeys.entrySet(),
                keyValue -> new CurrentKey(keyValue.getKey(), keyValue.getValue()))
        );

        currentKeysDAO.updateTotalRecords(stagedCurrentKeys.size() - noOfRecordsDeleted - entriesWithoutItems.size());
        stagedCurrentKeys.clear();
        entriesWithoutItems.clear();
    }

    private int removeRecordsWithKeys(Iterable<String> keySet) {
        int[] noOfRecordsDeletedPerBatch = currentKeysDAO.removeRecordWithKeys(keySet);
        return IntStream.of(noOfRecordsDeletedPerBatch).sum();
    }
}
