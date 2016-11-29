package uk.gov.register.providers;

import uk.gov.register.exceptions.HashDecodeException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class HashDecodeExceptionMapper implements ExceptionMapper<HashDecodeException> {
    @Override
    public Response toResponse(HashDecodeException exception) {
        return Response.status(400).entity("Hash decode exception: " + exception.getMessage()).build();
    }
}