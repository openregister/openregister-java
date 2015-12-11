package uk.gov.register.thymeleaf;

import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.organisation.client.GovukOrganisation;

import java.util.Optional;

public class AttributionView extends ThymeleafView {

    private final PublicBody custodian;

    private final Optional<GovukOrganisation.Details> custodianBranding;

    public AttributionView(RequestContext requestContext, PublicBody custodian, Optional<GovukOrganisation.Details> custodianBranding, String templateName) {
        super(requestContext, templateName);
        this.custodian = custodian;
        this.custodianBranding = custodianBranding;
    }

    @SuppressWarnings("unused, used by templates")
    public PublicBody getCustodian() {
        return custodian;
    }

    @SuppressWarnings("unused, used by templates")
    public Optional<GovukOrganisation.Details> getBranding() {
        return custodianBranding;
    }
}

