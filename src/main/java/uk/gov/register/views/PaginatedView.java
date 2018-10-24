package uk.gov.register.views;

import uk.gov.register.core.PublicBody;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.resources.Pagination;
import uk.gov.register.resources.RequestContext;

public class PaginatedView<T> extends AttributionView<T> {
    private final Pagination pagination;

    public PaginatedView(String templateName, RequestContext requestContext, PublicBody registry, RegisterReadOnly register, RegisterResolver registerResolver, Pagination pagination, T baseView) {
        super(templateName, requestContext, registry, register, registerResolver, baseView);
        this.pagination = pagination;
    }

    public Pagination getPagination() {
        return pagination;
    }
}
