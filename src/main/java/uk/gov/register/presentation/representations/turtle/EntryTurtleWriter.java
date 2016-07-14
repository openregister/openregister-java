package uk.gov.register.presentation.representations.turtle;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.presentation.config.RegisterDomainConfiguration;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.presentation.view.EntryView;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

@Provider
@Produces(ExtraMediaType.TEXT_TTL)
public class EntryTurtleWriter extends TurtleRepresentationWriter<EntryView> {

    @Inject
    public EntryTurtleWriter(RequestContext requestContext, RegisterDomainConfiguration registerDomainConfiguration, RegisterNameConfiguration registerNameConfiguration) {
        super(requestContext, registerDomainConfiguration, registerNameConfiguration);
    }

    @Override
    protected Model rdfModelFor(EntryView entryView) {
        Model model = ModelFactory.createDefaultModel();
        Property entryNumberProperty = model.createProperty(SPEC_PREFIX + "entry-number-field");
        Property entryTimestampProperty = model.createProperty(SPEC_PREFIX + "entry-timestamp-field");
        Property itemProperty = model.createProperty(SPEC_PREFIX + "item-resource");

        model.createResource(entryUri(entryView.getEntry().entryNumber).toString())
                .addProperty(entryNumberProperty, entryView.getEntry().entryNumber)
                .addProperty(entryTimestampProperty, entryView.getEntry().getTimestamp())
                .addProperty(itemProperty, model.createResource(itemUri(entryView.getEntry().getSha256hex()).toString()));

        model.setNsPrefix("register-metadata", SPEC_PREFIX);
        return model;
    }
}
