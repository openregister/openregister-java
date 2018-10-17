package uk.gov.register.views.representations.turtle;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import uk.gov.register.core.Entry;
import uk.gov.register.core.RegisterId;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.views.BlobView;
import uk.gov.register.views.representations.ExtraMediaType;
import uk.gov.register.views.v1.V1EntryView;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;
import java.net.URI;
import java.util.List;
import java.util.Map;

@Provider
@Produces(ExtraMediaType.TEXT_TTL)
public class RecordTurtleWriter extends TurtleRepresentationWriter<Map.Entry<Entry, List<BlobView>>> {

    @Inject
    public RecordTurtleWriter(javax.inject.Provider<RegisterId> registerIdProvider, RegisterResolver registerResolver) {
        super(registerIdProvider, registerResolver);
    }

    @Override
    protected Model rdfModelFor(Map.Entry<Entry, List<BlobView>> record) {
        Entry entry = record.getKey();
        V1EntryView entryView = new V1EntryView(entry);
        BlobView blobView = record.getValue().get(0);

        Model recordModel = ModelFactory.createDefaultModel();
        Model entryModel = new EntryTurtleWriter(registerIdProvider, registerResolver).rdfModelFor(entryView, false);
        Model itemModel = new BlobTurtleWriter(registerIdProvider, registerResolver).rdfModelFor(blobView);

        Resource recordResource = recordModel.createResource(recordUri(entry.getKey()).toString());
        addPropertiesToResource(recordResource, entryModel.getResource(entryUri(Integer.toString(entry.getEntryNumber())).toString()));
        addPropertiesToResource(recordResource, itemModel.getResource(blobUri(blobView.getBlobHash().encode()).toString()));

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
