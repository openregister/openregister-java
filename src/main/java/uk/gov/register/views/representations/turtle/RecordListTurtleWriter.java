package uk.gov.register.views.representations.turtle;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import uk.gov.register.configuration.RegisterTrackingConfiguration;
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
    private javax.inject.Provider<RegisterData> registerData;
    private RegisterTrackingConfiguration registerTrackingConfiguration;

    @Inject
    public RecordListTurtleWriter(RequestContext requestContext, ItemConverter itemConverter, javax.inject.Provider<RegisterData> registerData, RegisterTrackingConfiguration registerTrackingConfiguration, RegisterResolver registerResolver) {
        super(requestContext, registerData, registerResolver);
        this.itemConverter = itemConverter;
        this.registerData = registerData;
        this.registerTrackingConfiguration = registerTrackingConfiguration;
    }

    @Override
    protected Model rdfModelFor(RecordListView view) {
        Model model = ModelFactory.createDefaultModel();
        view.getRecords().stream().forEach(r -> model.add(new RecordTurtleWriter(requestContext, itemConverter, registerData, registerTrackingConfiguration, registerResolver).rdfModelFor(r)));
        return model;
    }
}
