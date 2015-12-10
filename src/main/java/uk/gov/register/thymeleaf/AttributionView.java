package uk.gov.register.thymeleaf;

import uk.gov.register.presentation.config.PublicBodiesConfiguration;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.resource.RequestContext;

public class AttributionView extends ThymeleafView {

    protected final PublicBodiesConfiguration publicBodiesConfiguration;

    public AttributionView(RequestContext requestContext, PublicBodiesConfiguration publicBodiesConfiguration, String templateName) {
        super(requestContext, templateName);
        this.publicBodiesConfiguration = publicBodiesConfiguration;
    }

    @SuppressWarnings("unused, used by templates")
    public PublicBody getCustodian() {
        return publicBodiesConfiguration.getPublicBody(getRegister().getRegistry());
    }
}

