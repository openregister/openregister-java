package uk.gov.register.presentation.view;

import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.presentation.RegisterData;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.presentation.representations.CsvRepresentation;
import uk.gov.register.presentation.resource.RequestContext;

import java.util.Optional;

public abstract class CsvRepresentationView<T> extends AttributionView {

    public CsvRepresentationView(RequestContext requestContext, PublicBody custodian, Optional<GovukOrganisation.Details> custodianBranding, String templateName, RegisterDomainConfiguration registerDomainConfiguration, RegisterData registerData) {
        super(requestContext, custodian, custodianBranding, templateName, registerDomainConfiguration, registerData);
    }

    public abstract CsvRepresentation<T> csvRepresentation();
}
