package uk.gov.register.views;

import uk.gov.register.core.PublicBody;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;

public class AttributionView<T> extends ThymeleafView {

    private final PublicBody registry;

    private final T baseView;

    public AttributionView(String templateName, RequestContext requestContext, PublicBody registry, RegisterReadOnly registerReadOnly, RegisterResolver registerResolver, T baseView) {
        super(requestContext, templateName, registerResolver, registerReadOnly);
        this.registry = registry;
        this.baseView = baseView;
    }

    @SuppressWarnings("unused, used by templates")
    public PublicBody getRegistry() {
        return registry;
    }


    public T getContent() {
        return baseView;
    }
}

