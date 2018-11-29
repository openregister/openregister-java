package uk.gov.register.util;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.objecthash.ObjectHash;
import uk.gov.objecthash.ObjectHashable;
import uk.gov.objecthash.SetValue;
import uk.gov.objecthash.StringValue;
import uk.gov.register.core.HashingAlgorithm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JsonToBlobHash {

    public static HashValue apply (JsonNode jsonNode){
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
        Stream<Map.Entry<String, JsonNode>> stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(fields, Spliterator.ORDERED), false);

        Map<String, ObjectHashable> data = new HashMap<>();
        stream.forEach(node -> {
            String key = node.getKey();
            Boolean valueIsArray = node.getValue().isArray();

            if(valueIsArray) {
                Stream<JsonNode> valueStream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(node.getValue().elements(), Spliterator.ORDERED), false);
                Set<ObjectHashable> values = valueStream
                        .filter(JsonNode::isTextual)
                        .filter(v -> !v.asText().equals(""))
                        .map(v -> new StringValue(v.asText()))
                        .collect(Collectors.toSet());

                if(!values.isEmpty()) {
                    data.put(key, new SetValue(values));
                }

            } else if (node.getValue().isTextual()) {
                String value = node.getValue().asText();
                if(!value.equals("")) {
                    data.put(key, new StringValue(value));
                }
            }
        }
        );

        String result = ObjectHash.toHexDigest(data);
        return new HashValue(HashingAlgorithm.SHA256, result);
    }
}

