package uk.gov.register.db;

import com.google.common.collect.Iterables;

import java.util.HashMap;
import java.util.stream.IntStream;

public class TransactionalRecordIndex extends AbstractRecordIndex {
    private final HashMap<String, Integer> stagedCurrentKeys;
    private final CurrentKeysUpdateDAO currentKeysDAO;

    public TransactionalRecordIndex(RecordQueryDAO recordQueryDAO, CurrentKeysUpdateDAO currentKeysDAO) {
        super(recordQueryDAO);
        this.currentKeysDAO = currentKeysDAO;
        stagedCurrentKeys = new HashMap<>();
    }

    @Override
    public void updateRecordIndex(String key, Integer entryNumber) {
        stagedCurrentKeys.put(key, entryNumber);
    }

    @Override
    public void checkpoint() {
        int[] noOfRecordsDeletedPerBatch = currentKeysDAO.removeRecordWithKeys(stagedCurrentKeys.keySet());
        int noOfRecordsDeleted = IntStream.of(noOfRecordsDeletedPerBatch).sum();
        currentKeysDAO.writeCurrentKeys(Iterables.transform(stagedCurrentKeys.entrySet(),
                keyValue -> new CurrentKey(keyValue.getKey(), keyValue.getValue()))
        );
        currentKeysDAO.updateTotalRecords(stagedCurrentKeys.size() - noOfRecordsDeleted);
        stagedCurrentKeys.clear();
    }
}
