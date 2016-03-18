package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import uk.gov.store.EntriesUpdateDAO;
import uk.gov.store.EntryStore;

import java.util.List;
import java.util.stream.Collectors;

public class LoadHandler implements Loader {
    private final CanonicalJsonMapper canonicalJsonMapper;
    private final EntriesUpdateDAO entriesUpdateDAO;
    private EntryStore entryStore;
    private boolean migrationInProcess;

    public LoadHandler(EntriesUpdateDAO entriesUpdateDAO, EntryStore entryStore, boolean migrationInProcess) {
        this.entriesUpdateDAO = entriesUpdateDAO;
        this.entryStore = entryStore;
        this.migrationInProcess = migrationInProcess;
        this.canonicalJsonMapper = new CanonicalJsonMapper();
    }

    @Override
    public void load(List<JsonNode> items) {
        List<byte[]> entriesAsBytes = items.stream()
                .map(singleEntry -> canonicalJsonMapper.writeToBytes(hashedEntry(singleEntry)))
                .collect(Collectors.toList());
        entriesUpdateDAO.add(entriesAsBytes);

        if (!migrationInProcess) {
            entryStore.load(Lists.transform(items, Item::new));
        }
    }

    private ObjectNode hashedEntry(JsonNode entryJsonNode) {
        ObjectNode jsonNode = JsonNodeFactory.instance.objectNode();
        jsonNode.put("hash", Digest.shasum(entryJsonNode.toString()));
        jsonNode.set("entry", entryJsonNode);
        return jsonNode;
    }

}
