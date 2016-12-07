package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.PublicBody;
import uk.gov.register.core.Record;
import uk.gov.register.core.RegisterData;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.resources.Pagination;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.views.representations.CsvRepresentation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecordListView extends OldAttributionView implements CsvRepresentationView {
    private final RegisterData registerData;
    private final RegisterResolver registerResolver;
    private RegisterTrackingConfiguration registerTrackingConfiguration;
    private Pagination pagination;
    private ItemConverter itemConverter;
    private List<Record> records;

    public RecordListView(RequestContext requestContext, PublicBody registry, Optional<GovukOrganisation.Details> branding, Pagination pagination, ItemConverter itemConverter, List<Record> records, RegisterData registerData, RegisterTrackingConfiguration registerTrackingConfiguration, RegisterResolver registerResolver) {
        super(requestContext, registry, branding, "records.html", registerData, registerTrackingConfiguration, registerResolver);
        this.pagination = pagination;
        this.itemConverter = itemConverter;
        this.records = records;
        this.registerData = registerData;
        this.registerTrackingConfiguration = registerTrackingConfiguration;
        this.registerResolver = registerResolver;
    }

    @JsonValue
    public Map<String, RecordView> recordsJson() {
        return getRecords().stream().collect(Collectors.toMap(RecordView::getPrimaryKey, r -> r));
    }

    public List<RecordView> getRecords() {
        return records.stream().map(r -> new RecordView(requestContext, getRegistry(), getBranding(), itemConverter, r, registerData, registerTrackingConfiguration, registerResolver)).collect(Collectors.toList());
    }

    public Pagination getPagination() {
        return pagination;
    }

    @Override
    public CsvRepresentation<Collection<RecordView>> csvRepresentation() {
        return new CsvRepresentation<>(Record.csvSchema(getRegister().getFields()), getRecords());
    }
}
