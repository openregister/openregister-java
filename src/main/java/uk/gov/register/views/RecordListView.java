package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.PublicBody;
import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.core.Record;
import uk.gov.register.core.RegisterData;
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
    private final RegisterData registerData;
    private RegisterTrackingConfiguration registerTrackingConfiguration;
    private Pagination pagination;
    private ItemConverter itemConverter;
    private List<Record> records;
    private final RegisterDomainConfiguration registerDomainConfiguration;

    public RecordListView(RequestContext requestContext, PublicBody custodian, Optional<GovukOrganisation.Details> custodianBranding, Pagination pagination, ItemConverter itemConverter, List<Record> records, RegisterDomainConfiguration registerDomainConfiguration, RegisterData registerData, RegisterTrackingConfiguration registerTrackingConfiguration) {
        super(requestContext, custodian, custodianBranding, "records.html", registerDomainConfiguration, registerData, registerTrackingConfiguration);
        this.pagination = pagination;
        this.itemConverter = itemConverter;
        this.records = records;
        this.registerDomainConfiguration = registerDomainConfiguration;
        this.registerData = registerData;
        this.registerTrackingConfiguration = registerTrackingConfiguration;
    }

    @JsonValue
    public Map<String, RecordView> recordsJson() {
        return getRecords().stream().collect(Collectors.toMap(RecordView::getPrimaryKey, r -> r));
    }

    public List<RecordView> getRecords() {
        return records.stream().map(r -> new RecordView(requestContext, getCustodian(), getBranding(), itemConverter, r, registerDomainConfiguration, registerData, registerTrackingConfiguration)).collect(Collectors.toList());
    }

    public Pagination getPagination() {
        return pagination;
    }

    @Override
    public CsvRepresentation<Collection<RecordView>> csvRepresentation() {
        return new CsvRepresentation<>(Record.csvSchema(getRegister().getFields()), getRecords());
    }
}
