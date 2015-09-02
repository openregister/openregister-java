package uk.gov.register.presentation.resource;

import uk.gov.register.thymeleaf.ThymeleafView;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ThrowableExceptionMapper extends ResourceBase implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable exception) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML)
                .entity(new ThymeleafView(httpServletRequest, httpServletResponse, servletContext, "500.html"))
                .build();
    }
}
