package uk.gov.register.views.representations.turtle;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import uk.gov.register.views.representations.ExtraMediaType;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.core.RegisterData;
import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.views.RecordListView;

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

    @Inject
    public RecordListTurtleWriter(RequestContext requestContext, ItemConverter itemConverter, RegisterDomainConfiguration registerDomainConfiguration, RegisterData registerData, RegisterNameConfiguration registerNameConfiguration) {
        super(requestContext, registerDomainConfiguration, registerNameConfiguration);
        this.itemConverter = itemConverter;
        this.registerDomainConfiguration = registerDomainConfiguration;
        this.registerData = registerData;
        this.registerNameConfiguration = registerNameConfiguration;
    }

    @Override
    protected Model rdfModelFor(RecordListView view) {
        Model model = ModelFactory.createDefaultModel();
        view.getRecords().stream().forEach(r -> model.add(new RecordTurtleWriter(requestContext, itemConverter, registerDomainConfiguration, registerData, registerNameConfiguration).rdfModelFor(r)));
        return model;
    }
}
