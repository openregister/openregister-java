package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.dropwizard.jackson.Jackson;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.presentation.EntryConverter;
import uk.gov.register.presentation.FieldValue;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.dao.Record;
import uk.gov.register.presentation.resource.RequestContext;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecordView extends AttributionView {
    private EntryConverter itemConverter;
    private final Record record;

    public RecordView(RequestContext requestContext, PublicBody custodian, Optional<GovukOrganisation.Details> custodianBranding, EntryConverter itemConverter, Record record) {
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
    public ObjectNode getRecordJson() throws JsonProcessingException {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        ObjectNode jsonNodes = objectMapper.convertValue(record.entry, ObjectNode.class);
        jsonNodes.setAll((ObjectNode) record.item.content.deepCopy());
        return jsonNodes;
    }

    public Map<String, FieldValue> getContent() {
        return record.item.getFieldsStream().collect(Collectors.toMap(Map.Entry::getKey, itemConverter::convert));
    }
}
