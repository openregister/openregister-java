package uk.gov.register.presentation.resource;

import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.ViewFactory;
import uk.gov.register.thymeleaf.ThymeleafView;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotFoundException;
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
            return viewFactory.badRequestExceptionView((BadRequestException) exception);
        }

        if (exception instanceof NotFoundException || exception instanceof NotAcceptableException) {
            return viewFactory.thymeleafView("404.html");
        }

        throw exception;
    }
}
