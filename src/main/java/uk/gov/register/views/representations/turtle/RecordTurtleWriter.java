package uk.gov.register.views.representations.turtle;

import org.apache.jena.rdf.model.*;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.core.Entry;
import uk.gov.register.core.RegisterResolver;
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

    private RegisterNameConfiguration registerNameConfiguration;

    @Inject
    public RecordTurtleWriter(RegisterNameConfiguration registerNameConfiguration, RegisterResolver registerResolver) {
        super(registerNameConfiguration, registerResolver);
        this.registerNameConfiguration = registerNameConfiguration;
        this.registerResolver = registerResolver;
    }

    @Override
    protected Model rdfModelFor(RecordView view) {
        Entry entry = view.getEntry();
        ItemView itemView = view.getItemView();

        Model recordModel = ModelFactory.createDefaultModel();
        Model entryModel = new EntryTurtleWriter(registerNameConfiguration, registerResolver).rdfModelFor(entry, false);
        Model itemModel = new ItemTurtleWriter(registerNameConfiguration, registerResolver).rdfModelFor(itemView);

        Resource recordResource = recordModel.createResource(recordUri(view.getPrimaryKey()).toString());
        addPropertiesToResource(recordResource, entryModel.getResource(entryUri(Integer.toString(entry.getEntryNumber())).toString()));
        addPropertiesToResource(recordResource, itemModel.getResource(itemUri(itemView.getItemHash().encode()).toString()));

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
