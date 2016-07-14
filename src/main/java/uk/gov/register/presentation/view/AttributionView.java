package uk.gov.register.presentation.view;

import uk.gov.register.presentation.RegisterData;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.config.RegisterDomainConfiguration;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.thymeleaf.ThymeleafView;

import java.util.Optional;

public class AttributionView extends ThymeleafView {

    private final PublicBody custodian;

    private final Optional<GovukOrganisation.Details> custodianBranding;

    public AttributionView(RequestContext requestContext, PublicBody custodian, Optional<GovukOrganisation.Details> custodianBranding, String templateName, RegisterDomainConfiguration registerDomainConfiguration, RegisterData registerData) {
        super(requestContext, templateName, registerData, registerDomainConfiguration);
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

