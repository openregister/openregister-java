package uk.gov.register.providers;

import uk.gov.register.thymeleaf.ThymeleafView;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ClientErrorExceptionMapper implements ExceptionMapper<ClientErrorException> {

    private ViewFactory viewFactory;

    @Inject
    public ClientErrorExceptionMapper(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
    }

    @Override
    public Response toResponse(ClientErrorException exception) {
        return Response
                .status(exception.getResponse().getStatus())
                .header(HttpHeaders.CONTENT_TYPE, ExtraMediaType.TEXT_HTML)
                .entity(getEntityFor(exception))
                .build();
    }

    private ThymeleafView getEntityFor(ClientErrorException exception) {
        if (exception instanceof BadRequestException) {
            return viewFactory.exceptionBadRequestView(exception.getMessage());
        }

        if (exception instanceof NotFoundException || exception instanceof NotAcceptableException) {
            return viewFactory.exceptionNotFoundView();
        }

        if (exception instanceof NotAllowedException) {
            return viewFactory.exceptionView("Method not allowed", "This method is not allowed");
        }

        int status = exception.getResponse().getStatus();
        return viewFactory.exceptionView(String.valueOf(status), exception.getMessage());
    }
}
