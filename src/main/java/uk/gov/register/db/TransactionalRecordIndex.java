package uk.gov.register.db;

import com.google.common.collect.Iterables;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

public class TransactionalRecordIndex extends AbstractRecordIndex {
    private final Set<String> stagedCurrentKeysForRemoval;
    private final HashMap<String, Integer> stagedCurrentKeysForAddition;
    private final CurrentKeysUpdateDAO currentKeysDAO;

    public TransactionalRecordIndex(RecordQueryDAO recordQueryDAO, CurrentKeysUpdateDAO currentKeysDAO) {
        super(recordQueryDAO);
        this.currentKeysDAO = currentKeysDAO;
        stagedCurrentKeysForAddition = new HashMap<>();
        stagedCurrentKeysForRemoval = new HashSet<>();
    }

    @Override
    public void updateRecordIndex(String key, Integer entryNumber) {
        stagedCurrentKeysForAddition.put(key, entryNumber);
    }

    @Override
    public void removeRecordIndex(String key) {
        if (stagedCurrentKeysForAddition.containsKey(key)) {
            stagedCurrentKeysForAddition.remove(key);
        }
        else {
            stagedCurrentKeysForRemoval.add(key);
        }
    }

    @Override
    public void checkpoint() {
        int noOfRecordsDeleted = removeRecordsWithKeys(stagedCurrentKeysForAddition.keySet()) + removeRecordsWithKeys(stagedCurrentKeysForRemoval);

        currentKeysDAO.writeCurrentKeys(Iterables.transform(stagedCurrentKeysForAddition.entrySet(),
                keyValue -> new CurrentKey(keyValue.getKey(), keyValue.getValue()))
        );
        currentKeysDAO.updateTotalRecords(stagedCurrentKeysForAddition.size() - noOfRecordsDeleted);
        stagedCurrentKeysForAddition.clear();
        stagedCurrentKeysForRemoval.clear();
    }

    private int removeRecordsWithKeys(Iterable<String> keySet) {
        int[] noOfRecordsDeletedPerBatch = currentKeysDAO.removeRecordWithKeys(keySet);
        return IntStream.of(noOfRecordsDeletedPerBatch).sum();
    }
}
