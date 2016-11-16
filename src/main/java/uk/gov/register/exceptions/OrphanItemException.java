package uk.gov.register.exceptions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.register.core.Item;

import java.util.Set;

public class OrphanItemException extends RuntimeException {

    private final ObjectNode errorJson;

    public OrphanItemException(String message, Set<Item> orphanItems) {
        super(message);
        errorJson = JsonNodeFactory.instance.objectNode();
        errorJson.put("message", message);
        ArrayNode orphanArray = errorJson.putArray("orphanItems");
        orphanItems.stream().forEach(i -> orphanArray.add(i.getContent()));
    }

    public ObjectNode getErrorJson() {
        return errorJson;
    }
}
