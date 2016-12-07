package uk.gov.register.views.representations.turtle;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.core.Entry;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.views.EntryListView;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

@Provider
@Produces(ExtraMediaType.TEXT_TTL)
public class EntryListTurtleWriter extends TurtleRepresentationWriter<EntryListView> {

    private RegisterNameConfiguration registerNameConfiguration;

    @Inject
    public EntryListTurtleWriter(RequestContext requestContext, RegisterNameConfiguration registerNameConfiguration, RegisterResolver registerResolver) {
        super(requestContext, registerNameConfiguration, registerResolver);
        this.registerNameConfiguration = registerNameConfiguration;
    }

    @Override
    protected Model rdfModelFor(EntryListView view) {
        Model model = ModelFactory.createDefaultModel();
        for (Entry entry : view.getEntries()) {
            model.add(new EntryTurtleWriter(requestContext, registerNameConfiguration, registerResolver).rdfModelFor(entry));
        }
        return model;
    }
}
