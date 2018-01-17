package uk.gov.register.views;

import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;

import javax.ws.rs.BadRequestException;

public class ExceptionView extends ThymeleafView {
    private final String heading;
    private final String message;

    public ExceptionView(RequestContext requestContext, String heading, String message, RegisterReadOnly register, RegisterResolver registerResolver) {
        super(requestContext, "exception.html", registerResolver, register);
        this.heading = heading;
        this.message = message;
    }

    public String getHeading() {
        return heading;
    }

    public String getMessage() {
        return message;
    }
}
