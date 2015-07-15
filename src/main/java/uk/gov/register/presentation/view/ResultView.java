package uk.gov.register.presentation.view;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.register.presentation.mapper.JsonObjectMapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ResultView extends AbstractView {
    public final Set entry;
    private final JsonNode jsonNode;

    public ResultView(String templateName, JsonNode jsonNode) {
        super(templateName);
        this.jsonNode = jsonNode;
        this.entry = ((Map) JsonObjectMapper.convert(jsonNode, Map.class).get("entry")).entrySet();
    }

    @Override
    public JsonNode getJsonNode() {
        return jsonNode;
    }

    public String getHash() {
        return jsonNode.get("hash").textValue();
    }
}
