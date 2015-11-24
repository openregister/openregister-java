package uk.gov.register.thymeleaf;

import uk.gov.register.presentation.config.PublicBodiesConfiguration;
import uk.gov.register.presentation.resource.RequestContext;

import javax.ws.rs.BadRequestException;

public class BadRequestExceptionView extends ThymeleafView {
    private final BadRequestException exception;

    public BadRequestExceptionView(RequestContext requestContext, PublicBodiesConfiguration publicBodiesConfiguration, BadRequestException exception) {
        super(requestContext, publicBodiesConfiguration, "400.html");
        this.exception = exception;
    }

    public BadRequestException getException() {
        return exception;
    }
}
