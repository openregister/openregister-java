package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.dao.Record;
import uk.gov.register.presentation.resource.RequestContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecordsView extends AttributionView {
    private List<Record> records;

    public RecordsView(RequestContext requestContext, PublicBody custodian, Optional<GovukOrganisation.Details> custodianBranding, List<Record> records) {
        super(requestContext, custodian, custodianBranding, "new-records.html");
        this.records = records;
    }

    @JsonValue
    public Map<String, JsonNode> getJson() {
        return records
                .stream()
                .map(Record::json)
                .collect(
                        Collectors.toMap(r -> r.get(requestContext.getRegisterPrimaryKey()).textValue(), r -> r)
                );
    }
}
