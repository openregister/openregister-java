package uk.gov.register.providers;

import uk.gov.register.exceptions.LoadException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class LoadExceptionMapper implements ExceptionMapper<LoadException> {

    @Override
    public Response toResponse(LoadException exception) {
        return Response.status(400).entity(exception.getMessage()).build();
    }
}
