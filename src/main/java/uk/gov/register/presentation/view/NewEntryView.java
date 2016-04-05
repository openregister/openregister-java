package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.dao.Entry;
import uk.gov.register.presentation.resource.RequestContext;

import java.util.Optional;

public class NewEntryView extends AttributionView {
    private Entry entry;

    public NewEntryView(RequestContext requestContext, PublicBody custodian, Optional<GovukOrganisation.Details> custodianBranding, Entry entry) {
        super(requestContext, custodian, custodianBranding, "new-entry.html");
        this.entry = entry;
    }

    @JsonValue
    public Entry getEntry() {
        return entry;
    }
}
