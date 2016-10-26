package uk.gov.register.views;

import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.PublicBody;
import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.core.RegisterData;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;

import java.util.Optional;

public class AttributionView extends ThymeleafView {

    private final PublicBody custodian;

    private final Optional<GovukOrganisation.Details> custodianBranding;

    public AttributionView(RequestContext requestContext, PublicBody custodian, Optional<GovukOrganisation.Details> custodianBranding, String templateName, RegisterDomainConfiguration registerDomainConfiguration, RegisterData registerData, RegisterTrackingConfiguration registerTrackingConfiguration) {
        super(requestContext, templateName, registerData, registerDomainConfiguration, registerTrackingConfiguration);
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

