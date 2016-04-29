package uk.gov.register.presentation.representations.turtle;

import org.apache.jena.rdf.model.*;
import uk.gov.register.presentation.ItemConverter;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.presentation.view.ItemView;
import uk.gov.register.presentation.view.NewEntryView;
import uk.gov.register.presentation.view.RecordView;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import java.util.Map;

@Provider
@Produces(ExtraMediaType.TEXT_TTL)
public class RecordTurtleWriter extends TurtleRepresentationWriter<RecordView> {

    private final ItemConverter itemConverter;

    @Inject
    public RecordTurtleWriter(RequestContext requestContext, ItemConverter itemConverter) {
        super(requestContext);
        this.itemConverter = itemConverter;
    }

    @Override
    protected Model rdfModelFor(RecordView view) {
        NewEntryView entryView = new NewEntryView(requestContext, view.getCustodian(), view.getBranding(), view.getRecord().entry);
        ItemView itemView = new ItemView(requestContext, view.getCustodian(), view.getBranding(), itemConverter, view.getRecord().item);

        Model recordModel = ModelFactory.createDefaultModel();
        Model entryModel = new EntryTurtleWriter(requestContext).rdfModelFor(entryView);
        Model itemModel = new ItemTurtleWiter(requestContext).rdfModelFor(itemView);

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
