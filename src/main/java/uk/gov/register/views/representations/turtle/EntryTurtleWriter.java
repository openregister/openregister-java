package uk.gov.register.views.representations.turtle;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.core.Entry;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

@Provider
@Produces(ExtraMediaType.TEXT_TTL)
public class EntryTurtleWriter extends TurtleRepresentationWriter<Entry> {

    @Inject
    public EntryTurtleWriter(RequestContext requestContext, RegisterNameConfiguration registerNameConfiguration, RegisterResolver registerResolver) {
        super(requestContext, registerNameConfiguration, registerResolver);
    }

    @Override
    protected Model rdfModelFor(Entry entry) {
        return rdfModelFor(entry, true);
    }

    protected Model rdfModelFor(Entry entry, boolean includeKey) {
        Model model = ModelFactory.createDefaultModel();
        Property entryNumberProperty = model.createProperty(SPEC_PREFIX + "entry-number-field");
        Property entryTimestampProperty = model.createProperty(SPEC_PREFIX + "entry-timestamp-field");
        Property itemProperty = model.createProperty(SPEC_PREFIX + "item-resource");

        String entryNumber = Integer.toString(entry.getEntryNumber());
        Resource resource = model.createResource(entryUri(entryNumber).toString())
                .addProperty(entryNumberProperty, entryNumber)
                .addProperty(entryTimestampProperty, entry.getTimestampAsISOFormat())
                .addProperty(itemProperty, model.createResource(itemUri(entry.getSha256hex().encode()).toString()));

        if (includeKey) {
            Property keyProperty = model.createProperty(SPEC_PREFIX + "key-field");
            resource.addProperty(keyProperty, entry.getKey());
        }

        model.setNsPrefix("register-metadata", SPEC_PREFIX);
        return model;
    }
}
