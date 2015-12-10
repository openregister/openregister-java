package uk.gov.register.thymeleaf;

import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.resource.RequestContext;

public class AttributionView extends ThymeleafView {

    private final PublicBody custodian;

    public AttributionView(RequestContext requestContext, PublicBody custodian, String templateName) {
        super(requestContext, templateName);
        this.custodian = custodian;
    }

    @SuppressWarnings("unused, used by templates")
    public PublicBody getCustodian() {
        return custodian;
    }
}

