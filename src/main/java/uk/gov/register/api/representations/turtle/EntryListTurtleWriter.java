package uk.gov.register.api.representations.turtle;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import uk.gov.register.api.representations.ExtraMediaType;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.core.RegisterData;
import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.views.EntryListView;
import uk.gov.register.views.EntryView;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import java.util.stream.Collectors;

@Provider
@Produces(ExtraMediaType.TEXT_TTL)
public class EntryListTurtleWriter extends TurtleRepresentationWriter<EntryListView> {

    private RegisterDomainConfiguration registerDomainConfiguration;
    private RegisterData registerData;
    private RegisterNameConfiguration registerNameConfiguration;

    @Inject
    public EntryListTurtleWriter(RequestContext requestContext, RegisterDomainConfiguration registerDomainConfiguration, RegisterData registerData, RegisterNameConfiguration registerNameConfiguration) {
        super(requestContext, registerDomainConfiguration, registerNameConfiguration);
        this.registerDomainConfiguration = registerDomainConfiguration;
        this.registerData = registerData;
        this.registerNameConfiguration = registerNameConfiguration;
    }

    @Override
    protected Model rdfModelFor(EntryListView view) {
        Model model = ModelFactory.createDefaultModel();
        for (EntryView entryView : view.getEntries().stream().map(e -> new EntryView(requestContext, view.getCustodian(), view.getBranding(), e, registerDomainConfiguration, registerData)).collect(Collectors.toList())) {
            model.add(new EntryTurtleWriter(requestContext, registerDomainConfiguration, registerNameConfiguration).rdfModelFor(entryView));
        }
        return model;
    }
}
