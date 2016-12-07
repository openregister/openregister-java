package uk.gov.register.views.representations.turtle;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.core.FieldValue;
import uk.gov.register.core.LinkValue;
import uk.gov.register.core.ListValue;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.views.ItemView;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;
import java.net.URI;
import java.util.Map;

@Provider
@Produces(ExtraMediaType.TEXT_TTL)
public class ItemTurtleWriter extends TurtleRepresentationWriter<ItemView> {

    @Inject
    public ItemTurtleWriter(RegisterNameConfiguration registerNameConfiguration, RegisterResolver registerResolver) {
        super(registerNameConfiguration, registerResolver);
    }

    @Override
    protected Model rdfModelFor(ItemView view) {
        Model model = ModelFactory.createDefaultModel();
        Resource resource = model.createResource(itemUri(view.getItemHash().encode()).toString());

        for (Map.Entry<String, FieldValue> field : view.getContent().entrySet()) {
            FieldRenderer fieldRenderer = new FieldRenderer(model.createProperty(fieldUri(field.getKey())));
            fieldRenderer.render(field.getValue(), resource);
        }
        model.setNsPrefix("field", fieldUri("/"));
        return model;
    }

    private String fieldUri(String key) {
        URI fieldBaseUri = registerResolver.baseUriFor("field");
        return UriBuilder.fromUri(fieldBaseUri).path("record").path(key).build().toString();
    }

    private class FieldRenderer {
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
                URI resolvedUri = registerResolver.getLinkResolver().resolve((LinkValue) value);
                resource.addProperty(fieldProperty, resource.getModel().createResource(resolvedUri.toString()));
            } else {
                resource.addProperty(fieldProperty, value.getValue());
            }
        }
    }
}
