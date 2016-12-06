package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.PublicBody;
import uk.gov.register.core.FieldValue;
import uk.gov.register.core.Item;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.views.representations.CsvRepresentation;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemView extends CsvRepresentationView {
    private ItemConverter itemConverter;
    private Item item;

    public ItemView(RequestContext requestContext, PublicBody registry, Optional<GovukOrganisation.Details> branding, ItemConverter itemConverter, Item item, RegisterTrackingConfiguration registerTrackingConfiguration, RegisterResolver registerResolver, RegisterReadOnly register) {
        super(requestContext, registry, branding, "item.html", registerTrackingConfiguration, registerResolver, register);
        this.itemConverter = itemConverter;
        this.item = item;
    }

    @JsonValue
    public Map<String, FieldValue> getContent() {
        return item.getFieldsStream().collect(Collectors.toMap(Map.Entry::getKey, itemConverter::convert));
    }

    @Override
    public CsvRepresentation<Map> csvRepresentation() {
        return new CsvRepresentation<>(Item.csvSchema(getRegister().getFields()), getContent());
    }

    public String getItemHash() {
        return item.getSha256hex().encode();
    }
}
