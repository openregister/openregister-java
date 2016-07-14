package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.presentation.RegisterData;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.config.RegisterDomainConfiguration;
import uk.gov.register.presentation.dao.Entry;
import uk.gov.register.presentation.representations.CsvRepresentation;
import uk.gov.register.presentation.resource.IPagination;
import uk.gov.register.presentation.resource.RequestContext;

import java.util.Collection;
import java.util.Optional;

public class EntryListView extends CsvRepresentationView {
    private IPagination pagination;
    private Collection<Entry> entries;
    private final Optional<String> recordKey;

    public EntryListView(RequestContext requestContext, IPagination pagination, PublicBody custodian, Optional<GovukOrganisation.Details> custodianBranding, Collection<Entry> entries, RegisterDomainConfiguration registerDomainConfiguration, RegisterData registerData) {
        super(requestContext, custodian, custodianBranding, "entries.html", registerDomainConfiguration, registerData);
        this.pagination = pagination;
        this.entries = entries;
        this.recordKey = Optional.empty();
    }

    public EntryListView(RequestContext requestContext, IPagination pagination, PublicBody custodian, Optional<GovukOrganisation.Details> custodianBranding, Collection<Entry> entries, String recordKey, RegisterDomainConfiguration registerDomainConfiguration, RegisterData registerData) {
        super(requestContext, custodian, custodianBranding, "entries.html", registerDomainConfiguration, registerData);
        this.pagination = pagination;
        this.entries = entries;
        this.recordKey = Optional.of(recordKey);
    }

    @JsonValue
    public Collection<Entry> getEntries() {
        return entries;
    }

    @SuppressWarnings("unused, used from templates")
    public IPagination getPagination() {
        return pagination;
    }

    @SuppressWarnings("unused, used from templates")
    public Optional<String> getRecordKey() { return recordKey; }

    @Override
    public CsvRepresentation<Collection<Entry>> csvRepresentation() {
        return new CsvRepresentation<>(Entry.csvSchema(), getEntries());
    }
}
