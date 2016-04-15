package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.presentation.EntryConverter;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.dao.Record;
import uk.gov.register.presentation.resource.RequestContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecordListView extends AttributionView {
    private EntryConverter itemConverter;
    private List<Record> records;

    public RecordListView(RequestContext requestContext, PublicBody custodian, Optional<GovukOrganisation.Details> custodianBranding, EntryConverter itemConverter, List<Record> records) {
        super(requestContext, custodian, custodianBranding, "new-records.html");
        this.itemConverter = itemConverter;
        this.records = records;
    }

    @JsonValue
    public Map<String, RecordView> getRecords() {
        return records
                .stream()
                .map(r -> new RecordView(requestContext, getCustodian(), getBranding(), itemConverter, r))
                .collect(
                        Collectors.toMap(RecordView::getPrimaryKey, r -> r)
                );
    }
}
