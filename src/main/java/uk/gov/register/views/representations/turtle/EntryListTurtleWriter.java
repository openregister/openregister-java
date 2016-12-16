package uk.gov.register.views.representations.turtle;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import uk.gov.register.core.Entry;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.views.EntryListView;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

@Provider
@Produces(ExtraMediaType.TEXT_TTL)
public class EntryListTurtleWriter extends TurtleRepresentationWriter<EntryListView> {

    @Inject
    public EntryListTurtleWriter(javax.inject.Provider<RegisterReadOnly> registerProvider, RegisterResolver registerResolver) {
        super(registerProvider, registerResolver);
    }

    @Override
    protected Model rdfModelFor(EntryListView view) {
        Model model = ModelFactory.createDefaultModel();
        for (Entry entry : view.getEntries()) {
            model.add(new EntryTurtleWriter(registerProvider, registerResolver).rdfModelFor(entry));
        }
        return model;
    }
}
