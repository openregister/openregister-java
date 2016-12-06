package uk.gov.register.views.representations.turtle;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import uk.gov.register.core.RegisterData;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.views.EntryView;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

@Provider
@Produces(ExtraMediaType.TEXT_TTL)
public class EntryTurtleWriter extends TurtleRepresentationWriter<EntryView> {

    @Inject
    public EntryTurtleWriter(RequestContext requestContext, javax.inject.Provider<RegisterData> registerData, RegisterResolver registerResolver) {
        super(requestContext, registerData, registerResolver);
    }

    @Override
    protected Model rdfModelFor(EntryView entryView) {
        Model model = ModelFactory.createDefaultModel();
        Property entryNumberProperty = model.createProperty(SPEC_PREFIX + "entry-number-field");
        Property entryTimestampProperty = model.createProperty(SPEC_PREFIX + "entry-timestamp-field");
        Property keyProperty = model.createProperty(SPEC_PREFIX + "key-field");
        Property itemProperty = model.createProperty(SPEC_PREFIX + "item-resource");

        String entryNumber = Integer.toString(entryView.getEntry().getEntryNumber());
        model.createResource(entryUri(entryNumber).toString())
                .addProperty(entryNumberProperty, entryNumber)
                .addProperty(entryTimestampProperty, entryView.getEntry().getTimestampAsISOFormat())
                .addProperty(keyProperty, entryView.getEntry().getKey())
                .addProperty(itemProperty, model.createResource(itemUri(entryView.getEntry().getSha256hex().encode()).toString()));

        model.setNsPrefix("register-metadata", SPEC_PREFIX);
        return model;
    }
}
