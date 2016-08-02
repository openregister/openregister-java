package uk.gov.register.views.representations.turtle;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.core.FieldValue;
import uk.gov.register.core.LinkValue;
import uk.gov.register.core.ListValue;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.views.ItemView;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import java.util.Map;

@Provider
@Produces(ExtraMediaType.TEXT_TTL)
public class ItemTurtleWriter extends TurtleRepresentationWriter<ItemView> {

    @Inject
    public ItemTurtleWriter(RequestContext requestContext, RegisterDomainConfiguration registerDomainConfiguration, RegisterNameConfiguration registerNameConfiguration) {
        super(requestContext, registerDomainConfiguration, registerNameConfiguration);
    }

    @Override
    protected Model rdfModelFor(ItemView view) {
        String itemFieldPrefix = fieldUri().toString();

        Model model = ModelFactory.createDefaultModel();
        Resource resource = model.createResource(itemUri(view.getSha256hex()).toString());

        for (Map.Entry<String, FieldValue> field : view.getContent().entrySet()) {
            FieldRenderer fieldRenderer = new FieldRenderer(model.createProperty(itemFieldPrefix + field.getKey()));
            fieldRenderer.render(field.getValue(), resource);
        }
        model.setNsPrefix("field", itemFieldPrefix);
        return model;
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
                renderScalar(value, resource);
            }
        }

        private void renderList(ListValue listValue, Resource resource) {
            for (FieldValue value : listValue) {
                renderScalar(value, resource);
            }
        }

        private void renderScalar(FieldValue value, Resource resource) {
            if (value.isLink()) {
                resource.addProperty(fieldProperty, resource.getModel().createResource(((LinkValue) value).link()));
            } else {
                resource.addProperty(fieldProperty, value.getValue());
            }
        }
    }
}
