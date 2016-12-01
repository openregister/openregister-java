package uk.gov.register.views.representations.turtle;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.LinkResolver;
import uk.gov.register.core.RegisterData;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.views.RecordListView;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

@Provider
@Produces(ExtraMediaType.TEXT_TTL)
public class RecordListTurtleWriter extends TurtleRepresentationWriter<RecordListView> {

    private final ItemConverter itemConverter;
    private RegisterData registerData;
    private RegisterNameConfiguration registerNameConfiguration;
    private RegisterTrackingConfiguration registerTrackingConfiguration;
    private LinkResolver linkResolver;

    @Inject
    public RecordListTurtleWriter(RequestContext requestContext, ItemConverter itemConverter, RegisterData registerData, RegisterNameConfiguration registerNameConfiguration, RegisterTrackingConfiguration registerTrackingConfiguration, LinkResolver linkResolver, RegisterResolver registerResolver) {
        super(requestContext, registerNameConfiguration, registerResolver);
        this.itemConverter = itemConverter;
        this.registerData = registerData;
        this.registerNameConfiguration = registerNameConfiguration;
        this.registerTrackingConfiguration = registerTrackingConfiguration;
        this.linkResolver = linkResolver;
    }

    @Override
    protected Model rdfModelFor(RecordListView view) {
        Model model = ModelFactory.createDefaultModel();
        view.getRecords().stream().forEach(r -> model.add(new RecordTurtleWriter(requestContext, itemConverter, registerData, registerNameConfiguration, registerTrackingConfiguration, linkResolver, registerResolver).rdfModelFor(r)));
        return model;
    }
}
