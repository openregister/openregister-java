package uk.gov.register.views.representations.turtle;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.RegisterData;
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
    private RegisterDomainConfiguration registerDomainConfiguration;
    private RegisterData registerData;
    private RegisterNameConfiguration registerNameConfiguration;
    private RegisterTrackingConfiguration registerTrackingConfiguration;

    @Inject
    public RecordListTurtleWriter(RequestContext requestContext, ItemConverter itemConverter, RegisterDomainConfiguration registerDomainConfiguration, RegisterData registerData, RegisterNameConfiguration registerNameConfiguration, RegisterTrackingConfiguration registerTrackingConfiguration) {
        super(requestContext, registerDomainConfiguration, registerNameConfiguration);
        this.itemConverter = itemConverter;
        this.registerDomainConfiguration = registerDomainConfiguration;
        this.registerData = registerData;
        this.registerNameConfiguration = registerNameConfiguration;
        this.registerTrackingConfiguration = registerTrackingConfiguration;
    }

    @Override
    protected Model rdfModelFor(RecordListView view) {
        Model model = ModelFactory.createDefaultModel();
        view.getRecords().stream().forEach(r -> model.add(new RecordTurtleWriter(requestContext, itemConverter, registerDomainConfiguration, registerData, registerNameConfiguration, registerTrackingConfiguration).rdfModelFor(r)));
        return model;
    }
}
