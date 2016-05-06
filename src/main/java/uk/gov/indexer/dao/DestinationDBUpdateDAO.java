package uk.gov.indexer.dao;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class DestinationDBUpdateDAO implements GetHandle, DBConnectionDAO {

    private final EntryUpdateDAO entryUpdateDAO;
    private final ItemUpdateDAO itemUpdateDAO;
    private final CurrentKeysUpdateDAO currentKeysUpdateDAO;

    public DestinationDBUpdateDAO() {
        Handle handle = getHandle();
        entryUpdateDAO = handle.attach(EntryUpdateDAO.class);
        entryUpdateDAO.ensureEntrySchemaInPlace();

        itemUpdateDAO = handle.attach(ItemUpdateDAO.class);
        itemUpdateDAO.ensureItemTableInPlace();

        currentKeysUpdateDAO = handle.attach(CurrentKeysUpdateDAO.class);
        currentKeysUpdateDAO.ensureRecordTablesInPlace();

    }

    public int lastReadEntryNumber() {
        return entryUpdateDAO.lastReadEntryNumber();
    }

    @Transaction(TransactionIsolationLevel.SERIALIZABLE)
    public void writeEntriesAndItemsInBatch(String registerName, List<Record> records) {
        entryUpdateDAO.writeBatch(Iterables.transform(records, record -> record.entry));
        itemUpdateDAO.writeBatch(Iterables.transform(records, record -> record.item));

        upsertInCurrentKeysTable(registerName, records);

        entryUpdateDAO.updateTotalEntries(records.size());
    }

    private void upsertInCurrentKeysTable(String registerName, List<Record> records) {
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
