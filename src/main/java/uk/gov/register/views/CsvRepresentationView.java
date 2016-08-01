package uk.gov.register.views;

import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.core.RegisterData;
import uk.gov.register.configuration.PublicBody;
import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.api.representations.CsvRepresentation;
import uk.gov.register.resources.RequestContext;

import java.util.Optional;

public abstract class CsvRepresentationView<T> extends AttributionView {

    public CsvRepresentationView(RequestContext requestContext, PublicBody custodian, Optional<GovukOrganisation.Details> custodianBranding, String templateName, RegisterDomainConfiguration registerDomainConfiguration, RegisterData registerData) {
        super(requestContext, custodian, custodianBranding, templateName, registerDomainConfiguration, registerData);
    }

    public abstract CsvRepresentation<T> csvRepresentation();
}
