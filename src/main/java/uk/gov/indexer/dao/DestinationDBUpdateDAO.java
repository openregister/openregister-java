package uk.gov.indexer.dao;

import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;
import uk.gov.indexer.ctserver.SignedTreeHead;
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

        indexedEntriesUpdateDAO = handle.attach(IndexedEntriesUpdateDAO.class);

        indexedEntriesUpdateDAO.ensureEntryTablesInPlace();
        if (!indexedEntriesUpdateDAO.indexedEntriesIndexExists()) {
            indexedEntriesUpdateDAO.createIndexedEntriesIndex();
        }

        signedTreeHeadDAO = handle.attach(SignedTreeHeadDAO.class);
        signedTreeHeadDAO.ensureTablesExists();
    }

    public int lastReadSerialNumber() {
        return indexedEntriesUpdateDAO.lastReadSerialNumber();
    }

    @Transaction(TransactionIsolationLevel.SERIALIZABLE)
    public void writeEntriesInBatch(int from, String registerName, FetchResult fetchResult) {

        SignedTreeHead signedTreeHead = fetchResult.getSignedTreeHead();

        signedTreeHeadDAO.updateSignedTree(signedTreeHead);

        List<Entry> entries;

        while (!(entries = fetchResult.getEntriesFn().get(from)).isEmpty()) {
            List<OrderedEntryIndex> orderedEntryIndex = entries.stream().map(Entry::dbEntry).collect(Collectors.toList());

            indexedEntriesUpdateDAO.writeBatch(orderedEntryIndex);

            upsertInCurrentKeysTable(registerName, orderedEntryIndex);

            from += entries.size();
        }
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
