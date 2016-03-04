package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.presentation.EntryConverter;
import uk.gov.register.presentation.RegisterDetail;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.resource.RequestContext;

import java.time.Instant;
import java.util.Optional;

public class RegisterDetailView extends AttributionView {
    private final EntryConverter entryConverter;

    private final int totalRecords;
    private final int totalEntries;
    private final int totalItems;
    private final Instant lastUpdated;

    public RegisterDetailView(
            PublicBody custodian,
            Optional<GovukOrganisation.Details> custodianBranding,
            RequestContext requestContext,
            EntryConverter entryConverter,
            int totalRecords,
            int totalEntries,
            int totalItems,
            Instant lastUpdated) {
        super(requestContext, custodian, custodianBranding, "");
        this.entryConverter = entryConverter;
        this.totalRecords = totalRecords;
        this.totalEntries = totalEntries;
        this.totalItems = totalItems;
        this.lastUpdated = lastUpdated;
    }

    @JsonValue
    public RegisterDetail getRegisterDetail() {
        return new RegisterDetail(
                getRegisterDomain(),
                totalRecords,
                totalEntries,
                totalItems,
                lastUpdated,
                getRegisterEntryView(entryConverter));
    }
}
