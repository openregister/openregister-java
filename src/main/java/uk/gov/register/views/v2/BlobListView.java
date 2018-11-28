package uk.gov.register.views.v2;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.dropwizard.jackson.Jackson;
import uk.gov.register.core.Field;
import uk.gov.register.core.Item;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.CsvRepresentationView;
import uk.gov.register.views.ItemView;
import uk.gov.register.views.representations.CsvRepresentation;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BlobListView implements CsvRepresentationView {
    private final Collection<Item> items;
    private final Map<String, Field> fieldsByName;
    private final ItemConverter itemConverter;
    private final ObjectMapper jsonObjectMapper = Jackson.newObjectMapper();

    public BlobListView(Collection<Item> items, final Map<String, Field> fieldsByName) {
        this.items = items;
        this.fieldsByName = fieldsByName;
        this.itemConverter = new ItemConverter();
    }

    @JsonValue
    public Map<HashValue, ItemView> getItems() {
        return this.items
                .stream()
                .collect(Collectors.toMap(item -> item.getSha256hex(), item -> new ItemView(item, fieldsByName, this.itemConverter)));
    }

    public static CsvSchema csvSchema(Iterable<String> fields) {
        CsvSchema.Builder schemaBuilder = new CsvSchema.Builder();
        schemaBuilder.addColumn("blob-hash", CsvSchema.ColumnType.STRING);
        for (String value : fields) {
            schemaBuilder.addColumn(value, CsvSchema.ColumnType.STRING);
        }
        return schemaBuilder.build();
    }

    public ArrayNode flatItemsJson() {
        final ArrayNode blobsArray = jsonObjectMapper.createArrayNode();
        getItems().forEach((key, value) -> {
            final ObjectNode valueNode = jsonObjectMapper.convertValue(value, ObjectNode.class);
            blobsArray.add(valueNode.put("blob-hash", jsonObjectMapper.convertValue(key, String.class)));
        });
        return blobsArray;
    }

    @Override
    public CsvRepresentation<ArrayNode> csvRepresentation() {
        return new CsvRepresentation<>(csvSchema(fieldsByName.keySet()), flatItemsJson());
    }
}
