package uk.gov.register.providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.exceptions.RSFParseException;
import uk.gov.register.serialization.RegisterResult;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RSFParseExceptionMapper implements ExceptionMapper<RSFParseException> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Response toResponse(RSFParseException exception) {
        return generate400Response(exception);
    }

    private Response generate400Response(RuntimeException exception) {
        logger.error("Failed to load RSF", exception);

        RegisterResult loadResult = RegisterResult.createFailResult("Failed to load RSF", exception);
        return Response.status(400).entity(loadResult).build();
    }
}
