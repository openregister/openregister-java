package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.presentation.ItemConverter;
import uk.gov.register.core.RegisterData;
import uk.gov.register.configuration.PublicBody;
import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.core.Record;
import uk.gov.register.views.representations.CsvRepresentation;
import uk.gov.register.resources.Pagination;
import uk.gov.register.resources.RequestContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecordListView extends CsvRepresentationView {
    private final RegisterData registerData;
    private Pagination pagination;
    private ItemConverter itemConverter;
    private List<Record> records;
    private final RegisterDomainConfiguration registerDomainConfiguration;

    public RecordListView(RequestContext requestContext, PublicBody custodian, Optional<GovukOrganisation.Details> custodianBranding, Pagination pagination, ItemConverter itemConverter, List<Record> records, RegisterDomainConfiguration registerDomainConfiguration, RegisterData registerData) {
        super(requestContext, custodian, custodianBranding, "records.html", registerDomainConfiguration, registerData);
        this.pagination = pagination;
        this.itemConverter = itemConverter;
        this.records = records;
        this.registerDomainConfiguration = registerDomainConfiguration;
        this.registerData = registerData;
    }

    @JsonValue
    public Map<String, RecordView> recordsJson() {
        return getRecords().stream().collect(Collectors.toMap(RecordView::getPrimaryKey, r -> r));
    }

    public List<RecordView> getRecords() {
        return records.stream().map(r -> new RecordView(requestContext, getCustodian(), getBranding(), itemConverter, r, registerDomainConfiguration, registerData)).collect(Collectors.toList());
    }

    public Pagination getPagination() {
        return pagination;
    }

    @Override
    public CsvRepresentation<Collection<RecordView>> csvRepresentation() {
        return new CsvRepresentation<>(Record.csvSchema(getRegister().getFields()), getRecords());
    }
}
