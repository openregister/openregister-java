package uk.gov.register.views;

import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;

import javax.ws.rs.BadRequestException;

public class BadRequestExceptionView extends ThymeleafView {
    private final BadRequestException exception;

    public BadRequestExceptionView(RequestContext requestContext, BadRequestException exception, RegisterReadOnly register, RegisterTrackingConfiguration registerTrackingConfiguration, RegisterResolver registerResolver) {
        super(requestContext, "400.html", registerTrackingConfiguration, registerResolver, register);
        this.exception = exception;
    }

    public BadRequestException getException() {
        return exception;
    }
}
