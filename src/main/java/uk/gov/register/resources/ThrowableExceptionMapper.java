package uk.gov.register.resources;

import org.glassfish.jersey.server.ParamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.api.representations.ExtraMediaType;
import uk.gov.register.presentation.view.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ThrowableExceptionMapper implements ExceptionMapper<Throwable> {

    public static final Logger LOGGER = LoggerFactory.getLogger(ThrowableExceptionMapper.class);

    private ViewFactory viewFactory;

    @Inject
    public ThrowableExceptionMapper(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
    }

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof ClientErrorException) {
            throw (ClientErrorException) exception;
        }

        if (exception instanceof ParamException.PathParamException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, ExtraMediaType.TEXT_HTML)
                    .entity(viewFactory.thymeleafView("404.html"))
                    .build();        }

        LOGGER.warn("Uncaught exception: {}", exception);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .header(HttpHeaders.CONTENT_TYPE, ExtraMediaType.TEXT_HTML)
                .entity(viewFactory.thymeleafView("500.html"))
                .build();
    }
}
