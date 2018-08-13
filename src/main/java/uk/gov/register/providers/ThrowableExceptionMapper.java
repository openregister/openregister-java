package uk.gov.register.providers;

import org.glassfish.jersey.server.ParamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.exceptions.FieldConversionException;
import uk.gov.register.exceptions.EnvironmentInconsistencyException;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ThrowableExceptionMapper implements ExceptionMapper<Throwable> {

    public static final Logger LOGGER = LoggerFactory.getLogger(ThrowableExceptionMapper.class);

    private final ViewFactory viewFactory;

    @Inject
    public ThrowableExceptionMapper(final ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
    }

    @Override
    public Response toResponse(final Throwable exception) {
        if (exception instanceof ParamException.PathParamException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, ExtraMediaType.TEXT_HTML)
                    .entity(viewFactory.exceptionNotFoundView())
                    .build();
        }

        if (exception instanceof FieldConversionException) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(HttpHeaders.CONTENT_TYPE, ExtraMediaType.TEXT_HTML)
                    .entity(viewFactory.exceptionFieldConversionView(exception.getMessage()))
                    .build();
        }

        if (exception instanceof EnvironmentInconsistencyException) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(HttpHeaders.CONTENT_TYPE, ExtraMediaType.TEXT_HTML)
                    .entity(viewFactory.exceptionInconsistencyView(exception.getMessage()))
                    .build();
        }

        LOGGER.error("Uncaught exception: {}", exception);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .header(HttpHeaders.CONTENT_TYPE, ExtraMediaType.TEXT_HTML)
                .entity(viewFactory.exceptionServerErrorView())
                .build();
    }
}
