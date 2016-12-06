package uk.gov.register.views.representations.turtle;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.views.EntryListView;
import uk.gov.register.views.EntryView;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import java.util.stream.Collectors;

@Provider
@Produces(ExtraMediaType.TEXT_TTL)
public class EntryListTurtleWriter extends TurtleRepresentationWriter<EntryListView> {

    private final RegisterReadOnly register;
    private RegisterNameConfiguration registerNameConfiguration;
    private RegisterTrackingConfiguration registerTrackingConfiguration;

    @Inject
    public EntryListTurtleWriter(RequestContext requestContext, RegisterNameConfiguration registerNameConfiguration, RegisterTrackingConfiguration registerTrackingConfiguration, RegisterResolver registerResolver, RegisterReadOnly register) {
        super(requestContext, registerNameConfiguration, registerResolver);
        this.registerNameConfiguration = registerNameConfiguration;
        this.registerTrackingConfiguration = registerTrackingConfiguration;
        this.register = register;
    }

    @Override
    protected Model rdfModelFor(EntryListView view) {
        Model model = ModelFactory.createDefaultModel();
        for (EntryView entryView : view.getEntries().stream().map(e -> new EntryView(requestContext, view.getRegistry(), view.getBranding(), e, registerTrackingConfiguration, registerResolver, register)).collect(Collectors.toList())) {
            model.add(new EntryTurtleWriter(requestContext, registerNameConfiguration, registerResolver).rdfModelFor(entryView));
        }
        return model;
    }
}
