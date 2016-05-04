package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.dao.Entry;
import uk.gov.register.presentation.representations.CsvRepresentation;
import uk.gov.register.presentation.resource.NewPagination;
import uk.gov.register.presentation.resource.Pagination;
import uk.gov.register.presentation.resource.RequestContext;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class EntryListView extends CsvRepresentationView {
    private NewPagination newPagination;
    private Pagination pagination;
    private Collection<Entry> entries;

    public EntryListView(RequestContext requestContext, Pagination pagination, PublicBody custodian, Optional<GovukOrganisation.Details> custodianBranding, List<Entry> entries) {
        super(requestContext, custodian, custodianBranding, "entries.html");
        this.pagination = pagination;
        this.entries = entries;
    }

    public EntryListView(RequestContext requestContext, NewPagination newPagination, PublicBody custodian, Optional<GovukOrganisation.Details> custodianBranding, Collection<Entry> entries, String template) {
        super(requestContext, custodian, custodianBranding, template);
        this.newPagination = newPagination;
        this.entries = entries;
    }

    @JsonValue
    public Collection<Entry> getEntries() {
        return entries;
    }

    @SuppressWarnings("unused, used from templates")
    public NewPagination getPagination() {
        return newPagination;
    }

    @Override
    public CsvRepresentation<Collection<Entry>> csvRepresentation() {
        return new CsvRepresentation<>(Entry.csvSchema(), getEntries());
    }
}
