package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.register.core.Field;
import uk.gov.register.core.Item;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.views.representations.CsvRepresentation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemListView implements CsvRepresentationView {
    private final Collection<Item> items;
    private final Map<String, Field> fieldsByName;
    private final ItemConverter itemConverter;

    public ItemListView(Collection<Item> items, final Map<String, Field> fieldsByName) {
        this.items = items;
        this.fieldsByName = fieldsByName;
        this.itemConverter = new ItemConverter();
    }

    @JsonValue
    public List<ItemView> getItems() {
        return this.items.stream().map(item -> new ItemView(
                item.getSha256hex(),
                itemConverter.convertItem(item, fieldsByName),
                fieldsByName.values()
        )).collect(Collectors.toList());
    }

    @Override
    public CsvRepresentation<Collection<ItemView>> csvRepresentation() {
        return new CsvRepresentation<>(Item.csvSchema(fieldsByName.keySet()), getItems());
    }
}
