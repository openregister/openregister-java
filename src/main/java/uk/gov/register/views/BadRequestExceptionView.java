package uk.gov.register.views;

import uk.gov.register.presentation.RegisterData;
import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;

import javax.ws.rs.BadRequestException;

public class BadRequestExceptionView extends ThymeleafView {
    private final BadRequestException exception;

    public BadRequestExceptionView(RequestContext requestContext, BadRequestException exception, RegisterDomainConfiguration registerDomainConfiguration, RegisterData registerData) {
        super(requestContext, "400.html", registerData, registerDomainConfiguration);
        this.exception = exception;
    }

    public BadRequestException getException() {
        return exception;
    }
}
