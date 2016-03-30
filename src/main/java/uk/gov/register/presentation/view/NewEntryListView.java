package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.dao.Entry;
import uk.gov.register.presentation.resource.RequestContext;

import java.util.List;
import java.util.Optional;

public class NewEntryListView extends AttributionView {
    private List<Entry> entries;

    public NewEntryListView(RequestContext requestContext, PublicBody custodian, Optional<GovukOrganisation.Details> custodianBranding, List<Entry> entries) {
        super(requestContext, custodian, custodianBranding, "new-entries.html");
        this.entries = entries;
    }

    @JsonValue
    public List<Entry> getEntries() {
        return entries;
    }
}
