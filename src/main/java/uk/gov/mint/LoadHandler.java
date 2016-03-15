package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.store.EntriesUpdateDAO;

import java.util.List;
import java.util.stream.Collectors;

public class LoadHandler implements Loader {
    private final CanonicalJsonMapper canonicalJsonMapper;
    private final EntriesUpdateDAO entriesUpdateDAO;

    public LoadHandler(EntriesUpdateDAO entriesUpdateDAO) {
        this.entriesUpdateDAO = entriesUpdateDAO;
        this.canonicalJsonMapper = new CanonicalJsonMapper();

        entriesUpdateDAO.ensureTableExists();
    }

    @Override
    public void load(List<JsonNode> entries) {
        List<byte[]> entriesAsBytes = entries.stream()
                .map(singleEntry -> canonicalJsonMapper.writeToBytes(hashedEntry(singleEntry)))
                .collect(Collectors.toList());
        entriesUpdateDAO.add(entriesAsBytes);
    }

    private ObjectNode hashedEntry(JsonNode entryJsonNode) {
        ObjectNode jsonNode = JsonNodeFactory.instance.objectNode();
        jsonNode.put("hash", Digest.shasum(entryJsonNode.toString()));
        jsonNode.set("entry", entryJsonNode);
        return jsonNode;
    }

}
