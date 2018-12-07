package uk.gov.register.views.v2;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import uk.gov.register.core.Field;
import uk.gov.register.core.Item;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.views.CsvRepresentationView;
import uk.gov.register.views.representations.CsvRepresentation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BlobListView implements CsvRepresentationView {
    private final Collection<Item> items;
    private final Map<String, Field> fieldsByName;
    private final ItemConverter itemConverter;

    public BlobListView(Collection<Item> items, final Map<String, Field> fieldsByName) {
        this.items = items;
        this.fieldsByName = fieldsByName;
        this.itemConverter = new ItemConverter();
    }

    @JsonValue
    public List<BlobView> getBlobs() {
        return this.items
                .stream()
                .map(item -> new BlobView(item, fieldsByName, this.itemConverter))
                .collect(Collectors.toList());
    }

    public static CsvSchema csvSchema(Iterable<String> fields) {
        CsvSchema.Builder schemaBuilder = new CsvSchema.Builder();
        schemaBuilder.addColumn("_id", CsvSchema.ColumnType.STRING);
        for (String value : fields) {
            schemaBuilder.addColumn(value, CsvSchema.ColumnType.STRING);
        }
        return schemaBuilder.build();
    }

    @Override
    public CsvRepresentation<Collection<BlobView>> csvRepresentation() {
        return new CsvRepresentation<>(csvSchema(fieldsByName.keySet()), getBlobs());
    }
}
