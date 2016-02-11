package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.presentation.Version;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.resource.RequestContext;

import java.util.List;
import java.util.Optional;

public class ListVersionView extends AttributionView {
    private final List<Version> versions;

    public ListVersionView(RequestContext requestContext, PublicBody custodian, Optional<GovukOrganisation.Details> custodianBranding, List<Version> versions) {
        super(requestContext, custodian, custodianBranding, "history.html");
        this.versions = versions;
    }

    @JsonValue
    public List<Version> getVersions() {
        return versions;
    }
}
