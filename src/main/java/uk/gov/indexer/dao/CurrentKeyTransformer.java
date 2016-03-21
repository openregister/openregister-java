package uk.gov.indexer.dao;

import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.indexer.JsonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CurrentKeyTransformer {

    public static List<CurrentKey> extractCurrentKeys(String registerName, List<OrderedEntryIndex> orderedEntryIndexes) {
        Map<String, Integer> currentKeys = new HashMap<>();
        orderedEntryIndexes.forEach(e1 -> currentKeys.put(getKey(registerName, e1.getEntry()), e1.getSerial_number()));
        return currentKeys
                .entrySet()
                .stream()
                .map(e -> new CurrentKey(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    public static List<CurrentKey> extractCurrentKeys(String registerName, List<Entry> entries, List<Item> items) {
        Map<String, Integer> currentKeys = new HashMap<>();
        entries.forEach(e -> currentKeys.put(
                getKey(registerName,
                        items.stream().collect(Collectors.toMap(i -> i.getItemHash(), i -> i.getContent()))
                                .get(e.getItemHash())),
                e.getEntryNumber()));
        return currentKeys
                .entrySet()
                .stream()
                .map(e -> new CurrentKey(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private static String getKey(String registerName, String entry) {
        JsonNode jsonNode = Jackson.jsonNodeOf(entry);
        return jsonNode.get("entry").get(registerName).textValue();
    }

    private static String getKey(String registerName, byte[] itemContent) {
        JsonNode jsonNode = JsonUtils.fromBytesToJsonNode(itemContent);
        return jsonNode.get(registerName).textValue();
    }
}
