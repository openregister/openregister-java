package uk.gov.register.presentation.resource;

import uk.gov.register.thymeleaf.ThymeleafView;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class NotFoundExceptionMapper extends ResourceBase implements ExceptionMapper<NotFoundException> {
    @Inject
    public NotFoundExceptionMapper(RequestContext requestContext) {
        super(requestContext);
    }

    public Response toResponse(NotFoundException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML)
                .entity(new ThymeleafView(requestContext, "404.html"))
                .build();
    }
}
