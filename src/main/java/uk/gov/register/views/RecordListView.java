package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.PublicBody;
import uk.gov.register.core.Record;
import uk.gov.register.core.RegisterReadOnly;
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

public class RecordListView extends CsvRepresentationView {
    private final RegisterResolver registerResolver;
    private final RegisterReadOnly register;
    private RegisterTrackingConfiguration registerTrackingConfiguration;
    private Pagination pagination;
    private ItemConverter itemConverter;
    private List<Record> records;

    public RecordListView(RequestContext requestContext, PublicBody registry, Optional<GovukOrganisation.Details> branding, Pagination pagination, ItemConverter itemConverter, List<Record> records, RegisterTrackingConfiguration registerTrackingConfiguration, RegisterResolver registerResolver, RegisterReadOnly register) {
        super(requestContext, registry, branding, "records.html", registerTrackingConfiguration, registerResolver, register);
        this.pagination = pagination;
        this.itemConverter = itemConverter;
        this.records = records;
        this.registerTrackingConfiguration = registerTrackingConfiguration;
        this.registerResolver = registerResolver;
        this.register = register;
    }

    @JsonValue
    public Map<String, RecordView> recordsJson() {
        return getRecords().stream().collect(Collectors.toMap(RecordView::getPrimaryKey, r -> r));
    }

    public List<RecordView> getRecords() {
        return records.stream().map(r -> new RecordView(requestContext, getRegistry(), getBranding(), itemConverter, r, registerTrackingConfiguration, registerResolver, register)).collect(Collectors.toList());
    }

    public Pagination getPagination() {
        return pagination;
    }

    @Override
    public CsvRepresentation<Collection<RecordView>> csvRepresentation() {
        return new CsvRepresentation<>(Record.csvSchema(getRegister().getFields()), getRecords());
    }
}
