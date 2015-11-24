package uk.gov.register.presentation.resource;

import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {
    private ViewFactory viewFactory;

    @Inject
    public BadRequestExceptionMapper(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
    }

    public Response toResponse(BadRequestException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .header(HttpHeaders.CONTENT_TYPE, ExtraMediaType.TEXT_HTML)
                .entity(viewFactory.thymeleafView("400.html"))
                .build();
    }
}
