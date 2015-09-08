package uk.gov.register.presentation.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.thymeleaf.ThymeleafView;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ThrowableExceptionMapper extends ResourceBase implements ExceptionMapper<Throwable> {

    public static final Logger LOGGER = LoggerFactory.getLogger(ThrowableExceptionMapper.class);

    @Inject
    public ThrowableExceptionMapper(RequestContext requestContext) {
        super(requestContext);
    }

    @Override
    public Response toResponse(Throwable exception) {
        LOGGER.warn("Uncaught exception: {}", exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML)
                .entity(new ThymeleafView(requestContext, "500.html"))
                .build();
    }
}
