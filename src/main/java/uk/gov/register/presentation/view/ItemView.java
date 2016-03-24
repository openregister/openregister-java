package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.presentation.EntryConverter;
import uk.gov.register.presentation.FieldValue;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.dao.Item;
import uk.gov.register.presentation.resource.RequestContext;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ItemView extends AttributionView {
    private EntryConverter itemConverter;
    private Item item;

    ItemView(RequestContext requestContext, PublicBody custodian, Optional<GovukOrganisation.Details> branding, EntryConverter itemConverter, Item item) {
        super(requestContext, custodian, branding, "item.html");
        this.itemConverter = itemConverter;
        this.item = item;
    }

    @JsonValue
    public Map<String, FieldValue> getContent() {
        Stream<Map.Entry<String, JsonNode>> fieldStream = StreamSupport.stream(((Iterable<Map.Entry<String, JsonNode>>) item.content::fields).spliterator(), false);
        return fieldStream.collect(Collectors.toMap(Map.Entry::getKey, itemConverter::convert));
    }
}
