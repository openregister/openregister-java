package uk.gov.register.api.representations.turtle;

import org.apache.jena.rdf.model.*;
import uk.gov.register.api.representations.ExtraMediaType;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.presentation.ItemConverter;
import uk.gov.register.presentation.RegisterData;
import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.presentation.view.ItemView;
import uk.gov.register.presentation.view.EntryView;
import uk.gov.register.presentation.view.RecordView;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import java.util.Map;

@Provider
@Produces(ExtraMediaType.TEXT_TTL)
public class RecordTurtleWriter extends TurtleRepresentationWriter<RecordView> {

    private final ItemConverter itemConverter;
    private final RegisterDomainConfiguration registerDomainConfiguration;
    private RegisterData registerData;
    private RegisterNameConfiguration registerNameConfiguration;

    @Inject
    public RecordTurtleWriter(RequestContext requestContext, ItemConverter itemConverter, RegisterDomainConfiguration registerDomainConfiguration, RegisterData registerData, RegisterNameConfiguration registerNameConfiguration) {
        super(requestContext, registerDomainConfiguration, registerNameConfiguration);
        this.itemConverter = itemConverter;
        this.registerDomainConfiguration = registerDomainConfiguration;
        this.registerData = registerData;
        this.registerNameConfiguration = registerNameConfiguration;
    }

    @Override
    protected Model rdfModelFor(RecordView view) {
        EntryView entryView = new EntryView(requestContext, view.getCustodian(), view.getBranding(), view.getRecord().entry, registerDomainConfiguration, registerData);
        ItemView itemView = new ItemView(requestContext, view.getCustodian(), view.getBranding(), itemConverter, view.getRecord().item, registerDomainConfiguration, registerData);

        Model recordModel = ModelFactory.createDefaultModel();
        Model entryModel = new EntryTurtleWriter(requestContext, registerDomainConfiguration, registerNameConfiguration).rdfModelFor(entryView);
        Model itemModel = new ItemTurtleWriter(requestContext, registerDomainConfiguration, registerNameConfiguration).rdfModelFor(itemView);

        Resource recordResource = recordModel.createResource(recordUri(view.getPrimaryKey()).toString());
        addPropertiesToResource(recordResource, entryModel.getResource(entryUri(entryView.getEntry().entryNumber).toString()));
        addPropertiesToResource(recordResource, itemModel.getResource(itemUri(itemView.getSha256hex()).toString()));

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
}
