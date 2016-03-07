package uk.gov.indexer.dao;

import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.indexer.ctserver.SignedTreeHead;
import uk.gov.indexer.fetchers.FetchResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public abstract class DestinationDBUpdateDAO implements GetHandle, DBConnectionDAO {
    private final Logger logger = LoggerFactory.getLogger(DestinationDBUpdateDAO.class);

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
    public long writeEntriesInBatch(int from, String registerName, FetchResult fetchResult) {
        long entriesWritten = 0;

        SignedTreeHead signedTreeHead = fetchResult.getSignedTreeHead();

        signedTreeHeadDAO.updateSignedTree(signedTreeHead);

        List<Entry> entries;

        while (!(entries = fetchResult.getEntryFetcher().fetch(from)).isEmpty()) {
            logger.info(String.format("Register '%s': Writing %s entries from index '%s' in transaction. total entries to write are: '%s'", registerName, entries.size(), from, signedTreeHead.getTree_size()));

            List<OrderedEntryIndex> orderedEntryIndex = entries.stream().map(Entry::dbEntry).collect(Collectors.toList());

            indexedEntriesUpdateDAO.writeBatch(orderedEntryIndex);

            upsertInCurrentKeysTable(registerName, orderedEntryIndex);

            from += entries.size();
            logger.info(String.format("Register '%s': Written '%d' more entries.", registerName, entries.size()));
            entriesWritten += entries.size();
        }
        return entriesWritten;
    }

    private void upsertInCurrentKeysTable(String registerName, List<OrderedEntryIndex> orderedEntryIndexes) {
        currentKeysUpdateDAO.removeRecordWithKeys(Lists.transform(orderedEntryIndexes, e -> getKey(registerName, e.getEntry())));
        currentKeysUpdateDAO.writeCurrentKeys(extractCurrentKeys(registerName, orderedEntryIndexes));
    }

    private Iterable<CurrentKey> extractCurrentKeys(String registerName, List<OrderedEntryIndex> orderedEntryIndexes) {
        Map<String, Integer> currentKeys = new HashMap<>();
        orderedEntryIndexes.forEach(e1 -> currentKeys.put(getKey(registerName, e1.getEntry()), e1.getSerial_number()));
        return currentKeys
                .entrySet()
                .stream()
                .map(e -> new CurrentKey(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private String getKey(String registerName, String entry) {
        JsonNode jsonNode = Jackson.jsonNodeOf(entry);
        return jsonNode.get("entry").get(registerName).textValue();
    }
}
