package uk.gov.register.views;

import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.configuration.HomepageContent;
import uk.gov.register.core.*;
import uk.gov.register.resources.RequestContext;

import java.util.Optional;

public class HomePageView extends AttributionView<Object> {
    private final HomepageContent homepageContent;

    public HomePageView(
            final PublicBody registry,
            final Optional<GovukOrganisation.Details> registryBranding,
            final RequestContext requestContext,
            final HomepageContent homepageContent,
            final RegisterResolver registerResolver,
            final RegisterReadOnly register) {
        super("home.html", requestContext, registry, registryBranding, register, registerResolver, null);
        this.homepageContent = homepageContent;
    }

    @SuppressWarnings("unused, used from template")
    public String getRegisterText() {
        return markdownProcessor.markdown(getRegister().getText());
    }

    @SuppressWarnings("unused, used from template")
    public HomepageContent getHomepageContent() {
        return homepageContent;
    }
}
