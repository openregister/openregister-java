package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.presentation.ItemConverter;
import uk.gov.register.presentation.FieldValue;
import uk.gov.register.presentation.LinkValue;
import uk.gov.register.presentation.ListValue;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.dao.Item;
import uk.gov.register.presentation.representations.CsvRepresentation;
import uk.gov.register.presentation.representations.RepresentationView;
import uk.gov.register.presentation.resource.RequestContext;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemView extends AttributionView implements RepresentationView {

    public static String ITEM_FIELD_PREFIX = "//field.%s/record/";

    private ItemConverter itemConverter;
    private Item item;

    public ItemView(RequestContext requestContext, PublicBody custodian, Optional<GovukOrganisation.Details> branding, ItemConverter itemConverter, Item item) {
        super(requestContext, custodian, branding, "item.html");
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

    @Override
    public Model turtleRepresentation() {
        String itemFieldPrefix = fieldUri().toString();

        Model model = ModelFactory.createDefaultModel();
        Resource resource = model.createResource(itemUri().toString());

        for (Map.Entry<String, FieldValue> field : getContent().entrySet()) {
            FieldRenderer fieldRenderer = new FieldRenderer(model.createProperty(itemFieldPrefix + field.getKey()));
            fieldRenderer.render(field.getValue(), resource);
        }
        model.setNsPrefix("field", itemFieldPrefix);
        return model;
    }

    public URI itemUri() {
        return UriBuilder.fromUri(getHttpServletRequest().getRequestURI()).replacePath(null).path("item").path(item.getSha256hex()).build();
    }

    private URI fieldUri() {
        String path = String.format(ITEM_FIELD_PREFIX, getRegisterDomain());
        return UriBuilder.fromPath(path).scheme(UriBuilder.fromPath(getHttpServletRequest().getRequestURI()).build().getScheme()).build();
    }

    private static class FieldRenderer {
        private final Property fieldProperty;

        public FieldRenderer(Property fieldProperty) {
            this.fieldProperty = fieldProperty;
        }

        public void render(FieldValue fieldO, Resource resource) {
            renderField(fieldO, resource);
        }

        private void renderField(FieldValue value, Resource resource) {
            if (value.isList()) {
                renderList((ListValue) value, resource);
            }
            else {
                resource.addProperty(fieldProperty, renderScalar(value));
            }
        }

        private void renderList(ListValue listValue, Resource resource) {
            for (FieldValue value : listValue) {
                resource.addProperty(fieldProperty, renderScalar(value));
            }
        }

        private String renderScalar(FieldValue value) {
            if (value.isLink()) {
                return ((LinkValue) value).link();
            } else {
                return value.getValue();
            }
        }
    }
}
