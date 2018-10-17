package uk.gov.register.views.representations.turtle;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import uk.gov.register.core.Entry;
import uk.gov.register.core.RegisterId;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.views.representations.ExtraMediaType;
import uk.gov.register.views.v1.V1EntryView;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

@Provider
@Produces(ExtraMediaType.TEXT_TTL)
public class EntryTurtleWriter extends TurtleRepresentationWriter<V1EntryView> {

    @Inject
    public EntryTurtleWriter(javax.inject.Provider<RegisterId> registerIdProvider, RegisterResolver registerResolver) {
        super(registerIdProvider, registerResolver);
    }

    @Override
    protected Model rdfModelFor(V1EntryView entry) {
        return rdfModelFor(entry, true);
    }

    protected Model rdfModelFor(V1EntryView entry, boolean includeKey) {
        Model model = ModelFactory.createDefaultModel();
        Property entryNumberProperty = model.createProperty(SPEC_PREFIX + "entry-number-field");
        Property entryTimestampProperty = model.createProperty(SPEC_PREFIX + "entry-timestamp-field");
        Property itemProperty = model.createProperty(SPEC_PREFIX + "item-resource");

        String entryNumber = Integer.toString(entry.getEntryNumber());
        Resource resource = model.createResource(entryUri(entryNumber).toString())
                .addProperty(entryNumberProperty, entryNumber)
                .addProperty(entryTimestampProperty, entry.getTimestampAsISOFormat())
                .addProperty(itemProperty, model.createResource(blobUri(entry.getBlobHashes().get(0).encode()).toString()));

        if (includeKey) {
            Property keyProperty = model.createProperty(SPEC_PREFIX + "key-field");
            resource.addProperty(keyProperty, entry.getKey());
        }

        model.setNsPrefix("register-metadata", SPEC_PREFIX);
        return model;
    }
}
