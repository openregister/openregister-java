package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.dropwizard.jackson.Jackson;
import org.apache.jena.rdf.model.*;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.presentation.ItemConverter;
import uk.gov.register.presentation.FieldValue;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.dao.Record;
import uk.gov.register.presentation.representations.CsvRepresentation;
import uk.gov.register.presentation.representations.RepresentationView;
import uk.gov.register.presentation.resource.RequestContext;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecordView extends AttributionView implements RepresentationView {
    private static final String RECORD_PREFIX = "//%1$s.%2$s/record/";

    private ItemConverter itemConverter;
    private final Record record;

    public RecordView(RequestContext requestContext, PublicBody custodian, Optional<GovukOrganisation.Details> custodianBranding, ItemConverter itemConverter, Record record) {
        super(requestContext, custodian, custodianBranding, "record.html");
        this.itemConverter = itemConverter;
        this.record = record;
    }

    @SuppressWarnings("unused, used from html templates")
    public String getPrimaryKey() {
        return record.item.content.get(requestContext.getRegisterPrimaryKey()).textValue();
    }

    @SuppressWarnings("unused, used to create the json representation of this class")
    @JsonValue
    public ObjectNode getRecordJson() {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        ObjectNode jsonNodes = objectMapper.convertValue(record.entry, ObjectNode.class);
        jsonNodes.setAll((ObjectNode) record.item.content.deepCopy());
        return jsonNodes;
    }

    public Map<String, FieldValue> getContent() {
        return record.item.getFieldsStream().collect(Collectors.toMap(Map.Entry::getKey, itemConverter::convert));
    }

    @SuppressWarnings("unused, used from html templates")
    public Optional<FieldValue> getField(String fieldName) {
        return Optional.ofNullable(getContent().get(fieldName));
    }

    @Override
    public CsvRepresentation<ObjectNode> csvRepresentation() {
        return new CsvRepresentation<>(Record.csvSchema(getRegister().getFields()), getRecordJson());
    }

    @Override
    public Model turtleRepresentation() {
        NewEntryView entryView = new NewEntryView(requestContext, getCustodian(), getBranding(), record.entry);
        ItemView itemView = new ItemView(requestContext, getCustodian(), getBranding(), itemConverter, record.item);

        Model recordModel = ModelFactory.createDefaultModel();
        Model entryModel = entryView.turtleRepresentation();
        Model itemModel = itemView.turtleRepresentation();

        Resource recordResource = recordModel.createResource(recordUri(getPrimaryKey()).toString());
        addPropertiesToResource(recordResource, entryModel.getResource(entryView.entryUri().toString()));
        addPropertiesToResource(recordResource, itemModel.getResource(itemView.itemUri().toString()));

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

    private URI recordUri(String primaryKey) {
        String path = String.format(RECORD_PREFIX, getRegisterId(), getRegisterDomain());
        return uriWithScheme(path).path(primaryKey).build();
    }

    private UriBuilder uriWithScheme(String path) {
        return UriBuilder.fromPath(path).scheme(requestContext.getScheme());
    }
}
