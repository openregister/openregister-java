package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.register.presentation.dao.Item;

public class ItemView {
    private Item item;

    public ItemView(Item item) {
        this.item = item;
    }

    @JsonValue
    public JsonNode getItem() {
        return item.content;
    }
}
