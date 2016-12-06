package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.dropwizard.jackson.Jackson;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.PublicBody;
import uk.gov.register.core.FieldValue;
import uk.gov.register.core.Record;
import uk.gov.register.core.RegisterData;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.views.representations.CsvRepresentation;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecordView extends CsvRepresentationView {
    private final String registerPrimaryKey;
    private ItemConverter itemConverter;
    private final Record record;

    public RecordView(RequestContext requestContext, PublicBody registry, Optional<GovukOrganisation.Details> branding, ItemConverter itemConverter, Record record, RegisterData registerData, RegisterTrackingConfiguration registerTrackingConfiguration, RegisterResolver registerResolver) {
        super(requestContext, registry, branding, "record.html", registerData, registerTrackingConfiguration, registerResolver);
        this.itemConverter = itemConverter;
        this.record = record;
        this.registerPrimaryKey = registerData.getRegister().getRegisterName();
    }

    public String getPrimaryKey() {
        return record.item.getContent().get(registerPrimaryKey).textValue();
    }

    @SuppressWarnings("unused, used to create the json representation of this class")
    @JsonValue
    public ObjectNode getRecordJson() {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        ObjectNode jsonNodes = objectMapper.convertValue(record.entry, ObjectNode.class);
        jsonNodes.remove("key");
        jsonNodes.setAll((ObjectNode) record.item.getContent().deepCopy());
        return jsonNodes;
    }

    public Map<String, FieldValue> getContent() {
        return record.item.getFieldsStream().collect(Collectors.toMap(Map.Entry::getKey, itemConverter::convert));
    }

    @SuppressWarnings("unused, used from html templates")
    public Optional<FieldValue> getField(String fieldName) {
        return Optional.ofNullable(getContent().get(fieldName));
    }

    public Record getRecord() {
        return record;
    }

    @Override
    public CsvRepresentation<ObjectNode> csvRepresentation() {
        return new CsvRepresentation<>(Record.csvSchema(getRegister().getFields()), getRecordJson());
    }
}
