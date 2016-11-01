package uk.gov.register.views;

import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.PublicBody;
import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.core.RegisterData;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.views.representations.CsvRepresentation;

import java.util.Optional;

public abstract class CsvRepresentationView<T> extends AttributionView {

    public CsvRepresentationView(RequestContext requestContext, PublicBody custodian, Optional<GovukOrganisation.Details> custodianBranding, String templateName, RegisterDomainConfiguration registerDomainConfiguration, RegisterData registerData, RegisterTrackingConfiguration registerTrackingConfiguration) {
        super(requestContext, custodian, custodianBranding, templateName, registerDomainConfiguration, registerData, registerTrackingConfiguration);
    }

    public abstract CsvRepresentation<T> csvRepresentation();
}
