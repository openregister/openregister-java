package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.register.presentation.EntryView;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.resource.Pagination;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.thymeleaf.AttributionView;

import java.util.List;

public class EntryListView extends AttributionView {
    private final List<EntryView> entries;
    private final Pagination pagination;

    EntryListView(
            RequestContext requestContext,
            List<EntryView> entries,
            Pagination pagination,
            PublicBody custodian,
            GovukOrganisation.Details custodianBranding,
            String templateName) {
        super(requestContext, custodian, custodianBranding, templateName);
        this.entries = entries;
        this.pagination = pagination;
    }

    @JsonValue
    public List<EntryView> getEntries() {
        return entries;
    }

    @SuppressWarnings("unused, used from templates")
    public Pagination getPagination() {
        return pagination;
    }
}
