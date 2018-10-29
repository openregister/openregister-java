package uk.gov.register.views.representations.turtle;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import uk.gov.register.core.Entry;
import uk.gov.register.core.RegisterId;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.views.EntryView;
import uk.gov.register.views.ItemView;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;
import java.net.URI;
import java.util.Map;

@Provider
@Produces(ExtraMediaType.TEXT_TTL)
public class RecordTurtleWriter extends TurtleRepresentationWriter<Map.Entry<Entry, ItemView>> {

    @Inject
    public RecordTurtleWriter(javax.inject.Provider<RegisterId> registerIdProvider, RegisterResolver registerResolver) {
        super(registerIdProvider, registerResolver);
    }

    @Override
    protected Model rdfModelFor(Map.Entry<Entry, ItemView> record) {
        Entry entry = record.getKey();
        ItemView itemView = record.getValue();

        Model recordModel = ModelFactory.createDefaultModel();
        EntryView entryView = new EntryView(entry);
        Model entryModel = new EntryTurtleWriter(registerIdProvider, registerResolver).rdfModelFor(entryView, false);
        Model itemModel = new ItemTurtleWriter(registerIdProvider, registerResolver).rdfModelFor(itemView);

        Resource recordResource = recordModel.createResource(recordUri(entry.getKey()).toString());
        addPropertiesToResource(recordResource, entryModel.getResource(entryUri(Integer.toString(entry.getEntryNumber())).toString()));
        addPropertiesToResource(recordResource, itemModel.getResource(itemUri(itemView.getItemHash().encode()).toString()));

        Map<String, String> prefixes = entryModel.getNsPrefixMap();
        prefixes.putAll(itemModel.getNsPrefixMap());

        recordModel.setNsPrefixes(prefixes);

        return recordModel;
    }

    private void addPropertiesToResource(Resource to, Resource from) {
        StmtIterator iterator = from.listProperties();
        while (iterator.hasNext()) {
            Statement statement = iterator.next();
            to.addProperty(statement.getPredicate(), statement.getObject());
        }
    }

    protected URI recordUri(String primaryKey) {
        return UriBuilder.fromUri(ourBaseUri()).path("records").path(primaryKey).build();
    }
}
