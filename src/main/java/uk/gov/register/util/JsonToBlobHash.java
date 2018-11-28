package uk.gov.register.util;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.objecthash.ObjectHash;
import uk.gov.objecthash.ObjectHashable;
import uk.gov.objecthash.StringValue;
import uk.gov.register.core.HashingAlgorithm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JsonToBlobHash {

    public static HashValue apply (JsonNode jsonNode){

        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
        Stream<Map.Entry<String, JsonNode>> stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(fields, Spliterator.ORDERED), false);
        String result = stream.map(node -> {
            String key = node.getKey();
                     Boolean valueIsArray = node.getValue().isArray();
                    String value = node.getValue().asText();
                    Map<String, ObjectHashable> data = new HashMap<>();
                    data.put(key, new StringValue(value));
                    return ObjectHash.toHexDigest(data);
                }
        ).collect(Collectors.joining(""));
        return new HashValue(HashingAlgorithm.SHA256, result);
    }


    }

