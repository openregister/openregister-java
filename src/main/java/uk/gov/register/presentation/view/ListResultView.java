package uk.gov.register.presentation.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.register.presentation.mapper.JsonObjectMapper;
import uk.gov.register.presentation.representations.ListResultJsonSerializer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@JsonSerialize(using = ListResultJsonSerializer.class)
public class ListResultView extends AbstractView<List<JsonNode>> {
    private final List<JsonNode> jsonNodes;

    public ListResultView(String templateName, List<JsonNode> jsonNodes) {
        super(templateName);
        this.jsonNodes = jsonNodes;
    }

    @Override
    public List<JsonNode> get() {
        return jsonNodes;
    }

    List<Set> getEntries() {
        return jsonNodes.stream().map(n -> JsonObjectMapper.convert(n, Map.class)).map(m -> {
            Map entry = (Map) m.get("entry");
            //noinspection unchecked
            entry.put("hash", m.get("hash"));
            return entry.entrySet();
        }).collect(Collectors.toList());
    }
}
