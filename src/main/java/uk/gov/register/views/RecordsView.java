package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterables;
import io.dropwizard.jackson.Jackson;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Field;
import uk.gov.register.core.Record;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.views.representations.CsvRepresentation;

import java.util.*;
import java.util.stream.Collectors;

public class RecordsView implements CsvRepresentationView {
    private final boolean displayEntryKeyColumn;
    private final boolean resolveAllItemLinks;

    private final Map<String,Field> fieldsByName;
    private final Map<Entry, List<ItemView>> recordMap;

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();

    public RecordsView(List<Record> records, Map<String, Field> fieldsByName, ItemConverter itemConverter,
                       boolean resolveAllItemLinks, boolean displayEntryKeyColumn) {
        this.displayEntryKeyColumn = displayEntryKeyColumn;
        this.resolveAllItemLinks = resolveAllItemLinks;
        this.fieldsByName = fieldsByName;
        this.recordMap = getItemViews(records, itemConverter);
    }

    public Map<Entry, List<ItemView>> getRecords() {
        return recordMap;
    }

    @SuppressWarnings("unused, used by the template")
    public List<ItemView> getRecordsSimple() {
        return recordMap.entrySet()
                .stream()
                .map(e -> new ItemSimpleView(e.getKey().getKey(), e.getValue().iterator().next()))
                .collect(Collectors.toList());

    }

    public Iterable<Field> getFields() {
        return fieldsByName.values();
    }

    @SuppressWarnings("unused, used by JSON renderer")
    @JsonValue
    public Map<String, JsonNode> getNestedRecordJson() {
        Map<String, JsonNode> records = new HashMap<>();
        recordMap.entrySet().forEach(record -> {
            ObjectNode jsonNode = getEntryJson(record.getKey());
            ArrayNode items = jsonNode.putArray("item");
            record.getValue().forEach(item -> items.add(getItemJson(item)));
            records.put(record.getKey().getKey(), jsonNode);
        });

        return records;
    }

    @Override
    public CsvRepresentation<ArrayNode> csvRepresentation() {
        Iterable<String> fieldNames = Iterables.transform(getFields(), f -> f.fieldName);
        return new CsvRepresentation<>(Record.csvSchema(fieldNames), getFlatRecordsJson());
    }

    protected ArrayNode getFlatRecordsJson() {
        ArrayNode flatRecords = objectMapper.createArrayNode();
        recordMap.entrySet().forEach(record -> record.getValue().forEach(item -> {
            ObjectNode jsonNodes = getEntryJson(record.getKey());
            jsonNodes.setAll(getItemJson(item));
            flatRecords.add(jsonNodes);
        }));

        return flatRecords;
    }

    @SuppressWarnings("unused, used by template")
    public boolean displayEntryKeyColumn() {
        return displayEntryKeyColumn;
    }

    @SuppressWarnings("unused, used by template")
    public boolean resolveAllItemLinks() {
        return resolveAllItemLinks;
    }

    private Map<Entry, List<ItemView>> getItemViews(Collection<Record> records, ItemConverter itemConverter) {
        Map<Entry, List<ItemView>> map = new LinkedHashMap<>();
        records.forEach(record -> {
            map.put(record.getEntry(), record.getItems().stream().map(item ->
                    new ItemView(item.getSha256hex(), itemConverter.convertItem(item, fieldsByName), getFields()))
                    .collect(Collectors.toList()));
        });
        return map;
    }

    private ObjectNode getEntryJson(Entry entry) {
        ObjectNode jsonNode = objectMapper.convertValue(entry, ObjectNode.class);
        jsonNode.remove("item-hash");
        return jsonNode;
    }

    private ObjectNode getItemJson(ItemView itemView) {
        ObjectNode jsonNode = objectMapper.convertValue(itemView, ObjectNode.class);
        return jsonNode;
    }
}
