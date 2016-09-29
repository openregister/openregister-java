package uk.gov.register.db;

import com.google.common.collect.Lists;
import uk.gov.register.core.Record;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DestinationDBUpdateDAO {
    protected final CurrentKeysUpdateDAO currentKeysUpdateDAO;

    @Inject
    protected DestinationDBUpdateDAO(CurrentKeysUpdateDAO currentKeysUpdateDAO) {
        this.currentKeysUpdateDAO = currentKeysUpdateDAO;
    }

    public void upsertInCurrentKeysTable(String registerName, List<Record> records) {
        int noOfRecordsDeleted = currentKeysUpdateDAO.removeRecordWithKeys(Lists.transform(records, r -> r.item.getKey(registerName)));
        List<CurrentKey> currentKeys = extractCurrentKeys(registerName, records);

        currentKeysUpdateDAO.writeCurrentKeys(currentKeys);
        currentKeysUpdateDAO.updateTotalRecords(currentKeys.size() - noOfRecordsDeleted);
    }

    private List<CurrentKey> extractCurrentKeys(String registerName, List<Record> records) {
        Map<String, Integer> currentKeys = new HashMap<>();
        records.forEach(r -> currentKeys.put(r.item.getKey(registerName), r.entry.getEntryNumber()));
        return currentKeys
                .entrySet()
                .stream()
                .map(e -> new CurrentKey(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }
}
