package uk.gov.register.db;

import com.google.common.collect.Lists;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;
import uk.gov.register.core.FatEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class DestinationDBUpdateDAO implements GetHandle {
    protected final CurrentKeysUpdateDAO currentKeysUpdateDAO;

    protected DestinationDBUpdateDAO() {
        Handle handle = getHandle();
        this.currentKeysUpdateDAO = handle.attach(CurrentKeysUpdateDAO.class);
        currentKeysUpdateDAO.ensureRecordTablesInPlace();

    }

    public void upsertInCurrentKeysTable(String registerName, List<FatEntry> records) {
        int noOfRecordsDeleted = currentKeysUpdateDAO.removeRecordWithKeys(Lists.transform(records, r -> r.item.getKey(registerName)));
        List<CurrentKey> currentKeys = extractCurrentKeys(registerName, records);

        currentKeysUpdateDAO.writeCurrentKeys(currentKeys);
        currentKeysUpdateDAO.updateTotalRecords(currentKeys.size() - noOfRecordsDeleted);
    }

    private List<CurrentKey> extractCurrentKeys(String registerName, List<FatEntry> records) {
        Map<String, Integer> currentKeys = new HashMap<>();
        records.forEach(r -> currentKeys.put(r.item.getKey(registerName), r.entry.getEntryNumber()));
        return currentKeys
                .entrySet()
                .stream()
                .map(e -> new CurrentKey(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }
}
