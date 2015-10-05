package uk.gov.indexer.dao;

import com.google.common.collect.Lists;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;

import java.util.List;
import java.util.stream.Collectors;


public abstract class DestinationDBUpdateDAO implements GetHandle, DBConnectionDAO {
    private final TotalRegisterEntriesUpdateDAO totalRegisterEntriesUpdateDAO;
    private final CurrentKeysUpdateDAO currentKeysUpdateDAO;
    private final IndexedEntriesUpdateDAO indexedEntriesUpdateDAO;

    public DestinationDBUpdateDAO() {
        Handle handle = getHandle();
        totalRegisterEntriesUpdateDAO = handle.attach(TotalRegisterEntriesUpdateDAO.class);

        currentKeysUpdateDAO = handle.attach(CurrentKeysUpdateDAO.class);

        indexedEntriesUpdateDAO = handle.attach(IndexedEntriesUpdateDAO.class);

        indexedEntriesUpdateDAO.ensureIndexedEntriesTableExists();

        currentKeysUpdateDAO.ensureCurrentKeysTableExists();

        totalRegisterEntriesUpdateDAO.ensureTotalEntriesInRegisterTableExists();
        totalRegisterEntriesUpdateDAO.initialiseTotalEntriesInRegisterIfRequired();
    }

    public int lastReadSerialNumber() {
        return indexedEntriesUpdateDAO.lastReadSerialNumber();
    }

    @Transaction(TransactionIsolationLevel.SERIALIZABLE)
    public void writeEntriesInBatch(String registerName, List<Entry> entries) {

        List<OrderedIndexEntry> orderedIndexEntry = entries.stream().map((entry) -> entry.dbEntry(registerName)).collect(Collectors.toList());
        indexedEntriesUpdateDAO.writeBatch(orderedIndexEntry);
        totalRegisterEntriesUpdateDAO.increaseTotalEntriesInRegisterCount(orderedIndexEntry.size());
        upsertInCurrentKeysTable(orderedIndexEntry);
    }

    private void upsertInCurrentKeysTable(List<OrderedIndexEntry> orderedIndexEntry) {
        List<String> allKeys = Lists.transform(orderedIndexEntry, e -> e.primaryKey);
        List<String> existingKeys = currentKeysUpdateDAO.getExistingKeys(String.join(",", allKeys));

        allKeys.removeAll(existingKeys);

        List<OrderedIndexEntry> newEntries = orderedIndexEntry.stream().filter(e -> allKeys.contains(e.primaryKey)).collect(Collectors.toList());
        List<OrderedIndexEntry> updateEntries = orderedIndexEntry.stream().filter(e -> existingKeys.contains(e.primaryKey)).collect(Collectors.toList());

        for (OrderedIndexEntry updateEntry : updateEntries) {
            currentKeysUpdateDAO.updateSerialNumber(updateEntry.serial_number, updateEntry.primaryKey);
        }
        currentKeysUpdateDAO.insertEntries(newEntries);
    }
}
