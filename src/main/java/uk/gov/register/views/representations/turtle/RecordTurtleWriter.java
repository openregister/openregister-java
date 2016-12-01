package uk.gov.register.views.representations.turtle;

import org.apache.jena.rdf.model.*;
import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.LinkResolver;
import uk.gov.register.core.RegisterData;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.views.EntryView;
import uk.gov.register.views.ItemView;
import uk.gov.register.views.RecordView;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;
import java.net.URI;
import java.util.Map;

@Provider
@Produces(ExtraMediaType.TEXT_TTL)
public class RecordTurtleWriter extends TurtleRepresentationWriter<RecordView> {

    private final ItemConverter itemConverter;
    private final RegisterDomainConfiguration registerDomainConfiguration;
    private RegisterData registerData;
    private RegisterNameConfiguration registerNameConfiguration;
    private RegisterTrackingConfiguration registerTrackingConfiguration;
    private LinkResolver linkResolver;

    @Inject
    public RecordTurtleWriter(RequestContext requestContext, ItemConverter itemConverter, RegisterDomainConfiguration registerDomainConfiguration, RegisterData registerData, RegisterNameConfiguration registerNameConfiguration, RegisterTrackingConfiguration registerTrackingConfiguration, LinkResolver linkResolver, RegisterResolver registerResolver) {
        super(requestContext, registerNameConfiguration, registerResolver);
        this.itemConverter = itemConverter;
        this.registerDomainConfiguration = registerDomainConfiguration;
        this.registerData = registerData;
        this.registerNameConfiguration = registerNameConfiguration;
        this.registerTrackingConfiguration = registerTrackingConfiguration;
        this.linkResolver = linkResolver;
        this.registerResolver = registerResolver;
    }

    @Override
    protected Model rdfModelFor(RecordView view) {
        EntryView entryView = new EntryView(requestContext, view.getCustodian(), view.getBranding(), view.getRecord().entry, registerDomainConfiguration, registerData, registerTrackingConfiguration);
        ItemView itemView = new ItemView(requestContext, view.getCustodian(), view.getBranding(), itemConverter, view.getRecord().item, registerDomainConfiguration, registerData, registerTrackingConfiguration);

        Model recordModel = ModelFactory.createDefaultModel();
        Model entryModel = new EntryTurtleWriter(requestContext, registerNameConfiguration, registerResolver).rdfModelFor(entryView);
        Model itemModel = new ItemTurtleWriter(requestContext, registerNameConfiguration, linkResolver, registerResolver).rdfModelFor(itemView);

        Resource recordResource = recordModel.createResource(recordUri(view.getPrimaryKey()).toString());
        addPropertiesToResource(recordResource, entryModel.getResource(entryUri(Integer.toString(entryView.getEntry().getEntryNumber())).toString()));
        addPropertiesToResource(recordResource, itemModel.getResource(itemUri(itemView.getItemHash()).toString()));

        Map<String, String> prefixes = entryModel.getNsPrefixMap();
        prefixes.putAll(itemModel.getNsPrefixMap());
        recordModel.setNsPrefixes(prefixes);

        return recordModel;
    }

    private void addPropertiesToResource(Resource to, Resource from) {
        StmtIterator iterator = from.listProperties();
        while(iterator.hasNext()) {
            Statement statement = iterator.next();
            to.addProperty(statement.getPredicate(), statement.getObject());
        }
    }

    protected URI recordUri(String primaryKey) {
        return UriBuilder.fromUri(ourBaseUri()).path("record").path(primaryKey).build();
    }
}
