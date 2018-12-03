package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.collect.Iterables;
import io.dropwizard.jackson.Jackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Field;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.exceptions.FieldConversionException;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.views.representations.CsvRepresentation;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RecordsView implements CsvRepresentationView {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordsView.class);

    private static final String END_OF_LINE = "\n";

    private final boolean displayEntryKeyColumn;
    private final boolean resolveAllItemLinks;

    private final Map<String, Field> fieldsByName;
    private final Map<Entry, ItemView> recordMap;

    private final ObjectMapper jsonObjectMapper = Jackson.newObjectMapper();

    public RecordsView(final List<Record> records, final Map<String, Field> fieldsByName, final ItemConverter itemConverter,
                       final boolean resolveAllItemLinks, final boolean displayEntryKeyColumn) throws FieldConversionException {
        this.displayEntryKeyColumn = displayEntryKeyColumn;
        this.resolveAllItemLinks = resolveAllItemLinks;
        this.fieldsByName = fieldsByName;
        recordMap = getItemViews(records, itemConverter);
    }

    public Map<Entry, ItemView> getRecords() {
        return recordMap;
    }

    @SuppressWarnings("unused, used by the template")
    public List<ItemSimpleView> getRecordsSimple() {
        return recordMap.entrySet()
                .stream()
                .map(e -> new ItemSimpleView(e.getKey().getKey(), e.getValue()))
                .collect(Collectors.toList());

    }

    public Iterable<Field> getFields() {
        return fieldsByName.values();
    }

    @SuppressWarnings("unused, used by JSON renderer")
    @JsonValue
    public Map<String, JsonNode> getNestedRecordJson() {
        final Map<String, JsonNode> records = new HashMap<>();
        recordMap.forEach((key, value) -> {
            final ObjectNode jsonNode = getEntryJson(key);
            final ArrayNode items = jsonNode.putArray("item");
            items.add(getItemJson(value));
            records.put(key.getKey(), jsonNode);
        });

        return records;
    }

    @Override
    public CsvRepresentation<ArrayNode> csvRepresentation() {
        final Iterable<String> fieldNames = Iterables.transform(getFields(), f -> f.fieldName);
        return new CsvRepresentation<>(RecordsView.csvSchema(fieldNames), getFlatRecordsJson());
    }

    protected ArrayNode getFlatRecordsJson() {
        final ArrayNode flatRecords = jsonObjectMapper.createArrayNode();
        recordMap.forEach((key, value) -> {
            final ObjectNode jsonNodes = getEntryJson(key);
            jsonNodes.setAll(getItemJson(value));
            flatRecords.add(jsonNodes);
        });

        return flatRecords;
    }

    @SuppressWarnings("unused, used by template")
    public boolean displayEntryKeyColumn() {
        return displayEntryKeyColumn;
    }

    @SuppressWarnings("unused, used by template")
    public static String urlEncodeKey(String key) throws UnsupportedEncodingException {
        return URLEncoder.encode(
                key, StandardCharsets.UTF_8.name());
    }

    @SuppressWarnings("unused, used by template")
    public boolean resolveAllItemLinks() {
        return resolveAllItemLinks;
    }


    private Map<Entry, ItemView> getItemViews(final Collection<Record> records, final ItemConverter itemConverter) throws FieldConversionException {
        final Map<Entry, ItemView> map = new LinkedHashMap<>();

        records.forEach(record -> {
            map.put(record.getEntry(), new ItemView(record.getItem(), fieldsByName, itemConverter));
        });
        return map;
    }

    private ObjectNode getEntryJson(final Entry entry) {
        final EntryView entryView = new EntryView(entry);
        final ObjectNode jsonNode = jsonObjectMapper.convertValue(entryView, ObjectNode.class);
        jsonNode.remove("item-hash");
        return jsonNode;
    }

    private ObjectNode getItemJson(final ItemView itemView) {
        return jsonObjectMapper.convertValue(itemView, ObjectNode.class);
    }

    public static CsvSchema csvSchema(Iterable<String> fields) {
        CsvSchema entrySchema = EntryView.csvSchemaWithOmittedFields(Arrays.asList("item-hash"));
        CsvSchema.Builder schemaBuilder = entrySchema.rebuild();

        for (Iterator<CsvSchema.Column> iterator = Item.csvSchema(fields).rebuild().getColumns(); iterator.hasNext();) {
            schemaBuilder.addColumn(iterator.next().getName(), CsvSchema.ColumnType.STRING);
        }
        return schemaBuilder.build();
    }
}
