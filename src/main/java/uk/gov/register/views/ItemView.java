package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.presentation.ItemConverter;
import uk.gov.register.core.FieldValue;
import uk.gov.register.core.RegisterData;
import uk.gov.register.configuration.PublicBody;
import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.core.Item;
import uk.gov.register.api.representations.CsvRepresentation;
import uk.gov.register.resources.RequestContext;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemView extends CsvRepresentationView {
    private ItemConverter itemConverter;
    private Item item;

    public ItemView(RequestContext requestContext, PublicBody custodian, Optional<GovukOrganisation.Details> branding, ItemConverter itemConverter, Item item, RegisterDomainConfiguration registerDomainConfiguration, RegisterData registerData) {
        super(requestContext, custodian, branding, "item.html", registerDomainConfiguration, registerData);
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

    public String getSha256hex() {
        return item.getSha256hex();
    }
}
