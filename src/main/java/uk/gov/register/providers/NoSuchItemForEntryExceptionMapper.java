package uk.gov.register.providers;

import uk.gov.register.exceptions.NoSuchItemForEntryException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NoSuchItemForEntryExceptionMapper implements ExceptionMapper<NoSuchItemForEntryException> {

    @Override
    public Response toResponse(NoSuchItemForEntryException exception) {
        return Response.status(400).entity(exception.getMessage()).build();
    }
}
