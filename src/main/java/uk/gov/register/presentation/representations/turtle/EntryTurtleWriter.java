package uk.gov.register.presentation.representations.turtle;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.presentation.view.NewEntryView;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

@Provider
@Produces(ExtraMediaType.TEXT_TTL)
public class EntryTurtleWriter extends TurtleRepresentationWriter<NewEntryView> {

    @Inject
    public EntryTurtleWriter(RequestContext requestContext) {
        super(requestContext);
    }

    @Override
    protected Model rdfModelFor(NewEntryView newEntryView) {
        Model model = ModelFactory.createDefaultModel();
        Property entryNumberProperty = model.createProperty(SPEC_PREFIX + "entry-number-field");
        Property entryTimestampProperty = model.createProperty(SPEC_PREFIX + "entry-timestamp-field");
        Property itemProperty = model.createProperty(SPEC_PREFIX + "item-resource");

        model.createResource(entryUri(newEntryView.getEntry().entryNumber).toString())
                .addProperty(entryNumberProperty, newEntryView.getEntry().entryNumber)
                .addProperty(entryTimestampProperty, newEntryView.getEntry().getTimestamp())
                .addProperty(itemProperty, model.createResource(itemUri(newEntryView.getEntry().getSha256hex()).toString()));

        model.setNsPrefix("register-metadata", SPEC_PREFIX);
        return model;
    }
}
