package uk.gov.indexer.dao;

import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;
import uk.gov.indexer.fetchers.FetchResult;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public abstract class DestinationDBUpdateDAO implements GetHandle, DBConnectionDAO {
    private final CurrentKeysUpdateDAO currentKeysUpdateDAO;
    private final IndexedEntriesUpdateDAO indexedEntriesUpdateDAO;
    private final SignedTreeHeadDAO signedTreeHeadDAO;

    public DestinationDBUpdateDAO() {
        Handle handle = getHandle();

        currentKeysUpdateDAO = handle.attach(CurrentKeysUpdateDAO.class);

        currentKeysUpdateDAO.ensureRecordTablesInPlace();

        signedTreeHeadDAO = handle.attach(SignedTreeHeadDAO.class);
        signedTreeHeadDAO.ensureTablesExists();

        indexedEntriesUpdateDAO = handle.attach(IndexedEntriesUpdateDAO.class);

        indexedEntriesUpdateDAO.ensureEntryTablesInPlace();
        if (!indexedEntriesUpdateDAO.indexedEntriesIndexExists()) {
            indexedEntriesUpdateDAO.createIndexedEntriesIndex();
        }
    }

    public int lastReadSerialNumber() {
        return indexedEntriesUpdateDAO.lastReadSerialNumber();
    }

    @Transaction(TransactionIsolationLevel.SERIALIZABLE)
    public void writeEntriesInBatch(String registerName, FetchResult fetchResult) {

        signedTreeHeadDAO.updateSignedTree(fetchResult.getSignedTreeHead());

        List<OrderedEntryIndex> orderedEntryIndex = fetchResult.getEntries().stream().map(Entry::dbEntry).collect(Collectors.toList());
        indexedEntriesUpdateDAO.writeBatch(orderedEntryIndex);

        upsertInCurrentKeysTable(registerName, orderedEntryIndex);
    }

    private void upsertInCurrentKeysTable(String registerName, List<OrderedEntryIndex> orderedEntryIndexes) {
        List<String> allKeys = Lists.transform(orderedEntryIndexes, e -> getKey(registerName, e.getEntry()));

        Set<String> existingKeys = currentKeysUpdateDAO.getExistingKeys(allKeys);

        orderedEntryIndexes.forEach(e -> {
                    String key = getKey(registerName, e.getEntry());
                    if (existingKeys.contains(key)) {
                        currentKeysUpdateDAO.updateSerialNumber(e.getSerial_number(), key);
                    } else {
                        currentKeysUpdateDAO.insertNewKey(e.getSerial_number(), key);
                        existingKeys.add(key);
                    }
                }
        );
    }

    private String getKey(String registerName, String entry) {
        JsonNode jsonNode = Jackson.jsonNodeOf(entry);
        return jsonNode.get("entry").get(registerName).textValue();
    }
}
