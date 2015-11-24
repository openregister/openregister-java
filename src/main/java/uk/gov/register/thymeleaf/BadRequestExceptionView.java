package uk.gov.register.thymeleaf;

import uk.gov.register.presentation.resource.RequestContext;

import javax.ws.rs.BadRequestException;

public class BadRequestExceptionView extends ThymeleafView {
    private final BadRequestException exception;

    public BadRequestExceptionView(RequestContext requestContext, BadRequestException exception) {
        super(requestContext, "400.html");
        this.exception = exception;
    }

    public BadRequestException getException() {
        return exception;
    }
}
