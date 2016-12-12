package uk.gov.register.db;

import com.google.common.collect.Iterables;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Record;
import uk.gov.register.core.RecordIndex;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class TransactionalRecordIndex implements RecordIndex {
    private final RecordQueryDAO recordQueryDAO;
    private final HashMap<String, Integer> stagedCurrentKeys;
    private final CurrentKeysUpdateDAO currentKeysDAO;

    public TransactionalRecordIndex(RecordQueryDAO recordQueryDAO, CurrentKeysUpdateDAO currentKeysDAO) {
        this.recordQueryDAO = recordQueryDAO;
        this.currentKeysDAO = currentKeysDAO;
        stagedCurrentKeys = new HashMap<>();
    }

    @Override
    public void updateRecordIndex(String key, Integer entryNumber) {
        stagedCurrentKeys.put(key, entryNumber);
    }

    @Override
    public Optional<Record> getRecord(String key) {
        checkpoint();
        return recordQueryDAO.findByPrimaryKey(key);
    }

    @Override
    public int getTotalRecords() {
        checkpoint();
        return recordQueryDAO.getTotalRecords();
    }

    @Override
    public List<Record> getRecords(int limit, int offset) {
        checkpoint();
        return recordQueryDAO.getRecords(limit, offset);
    }

    @Override
    public List<Record> findMax100RecordsByKeyValue(String key, String value) {
        checkpoint();
        return recordQueryDAO.findMax100RecordsByKeyValue(key, value);
    }

    @Override
    public Collection<Entry> findAllEntriesOfRecordBy(String registerName, String key) {
        checkpoint();
        return recordQueryDAO.findAllEntriesOfRecordBy(registerName, key);
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
