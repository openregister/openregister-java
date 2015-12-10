package uk.gov.register.thymeleaf;

import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.presentation.view.GovukOrganisation;

public class AttributionView extends ThymeleafView {

    private final PublicBody custodian;

    private final GovukOrganisation.Details custodianBranding;

    public AttributionView(RequestContext requestContext, PublicBody custodian, GovukOrganisation.Details custodianBranding, String templateName) {
        super(requestContext, templateName);
        this.custodian = custodian;
        this.custodianBranding = custodianBranding;
    }

    @SuppressWarnings("unused, used by templates")
    public PublicBody getCustodian() {
        return custodian;
    }

    @SuppressWarnings("unused, used by templates")
    public GovukOrganisation.Details getBranding() {
        return custodianBranding;
    }
}

