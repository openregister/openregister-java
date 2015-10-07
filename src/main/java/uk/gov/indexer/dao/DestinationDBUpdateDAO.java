package uk.gov.indexer.dao;

import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterables;
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

        List<OrderedEntryIndex> orderedEntryIndex = entries.stream().map(Entry::dbEntry).collect(Collectors.toList());
        indexedEntriesUpdateDAO.writeBatch(orderedEntryIndex);
        totalRegisterEntriesUpdateDAO.increaseTotalEntriesInRegisterCount(orderedEntryIndex.size());
        upsertInCurrentKeysTable(registerName, orderedEntryIndex);
    }

    private void upsertInCurrentKeysTable(String registerName, List<OrderedEntryIndex> orderedEntryIndexes) {

        List<String> allKeys = Lists.transform(orderedEntryIndexes, e -> getKey(registerName, e.getEntry()));

        List<String> existingKeys = currentKeysUpdateDAO.getExistingKeys(String.join(",", allKeys));

        Iterable<OrderedEntryIndex> newEntries = Iterables.filter(orderedEntryIndexes, e -> !existingKeys.contains(getKey(registerName, e.getEntry())));

        orderedEntryIndexes.forEach(e -> currentKeysUpdateDAO.updateSerialNumber(
                        e.getSerial_number(),
                        getKey(registerName, e.getEntry())
                )
        );

        currentKeysUpdateDAO.insertEntries(
                Iterables.transform(newEntries,
                        e -> new CurrentKey(
                                e.getSerial_number(),
                                getKey(registerName, e.getEntry())
                        )
                )
        );
    }

    private String getKey(String registerName, String entry) {
        JsonNode jsonNode = Jackson.jsonNodeOf(entry);
        return jsonNode.get("entry").get(registerName).textValue();
    }
}
