package uk.gov.register.providers;

import uk.gov.register.exceptions.RootHashAssertionException;
import uk.gov.register.exceptions.SerializationFormatValidationException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RootHashAssertionExceptionMapper implements ExceptionMapper<RootHashAssertionException> {

    @Override
    public Response toResponse(RootHashAssertionException exception) {
        return Response.status(409).entity("Root assertion exception: " + exception.getMessage()).build();

    }
}