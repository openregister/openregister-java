package uk.gov.register.views;

import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.PublicBody;
import uk.gov.register.core.RegisterData;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;

import java.util.Optional;

public class AttributionView<T> extends ThymeleafView {

    private final PublicBody registry;

    private final Optional<GovukOrganisation.Details> registryBranding;
    private final T baseView;

    public AttributionView(String templateName, RequestContext requestContext, PublicBody registry, Optional<GovukOrganisation.Details> registryBranding, RegisterData registerData, RegisterTrackingConfiguration registerTrackingConfiguration, RegisterResolver registerResolver, T baseView) {
        super(requestContext, templateName, registerData, registerTrackingConfiguration, registerResolver);
        this.registry = registry;
        this.registryBranding = registryBranding;
        this.baseView = baseView;
    }

    @SuppressWarnings("unused, used by templates")
    public PublicBody getRegistry() {
        return registry;
    }

    @SuppressWarnings("unused, used by templates")
    public Optional<GovukOrganisation.Details> getBranding() {
        return registryBranding;
    }

    public T getContent() {
        return baseView;
    }
}

