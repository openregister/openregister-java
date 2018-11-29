package uk.gov.register.util;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.objecthash.ObjectHash;
import uk.gov.objecthash.ObjectHashable;
import uk.gov.objecthash.SetValue;
import uk.gov.objecthash.StringValue;
import uk.gov.register.core.HashingAlgorithm;

import java.util.HashMap;
import java.util.HashSet;
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
                Set<ObjectHashable> values = new HashSet<>();
                node.getValue().elements().forEachRemaining(value -> values.add(new StringValue(value.asText())));
                SetValue setValue = new SetValue(values);
                data.put(key, setValue);
            } else {
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

