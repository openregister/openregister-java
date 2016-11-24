package uk.gov.register.providers;

import uk.gov.register.exceptions.SerializationFormatValidationException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class SerializationFormatValidationExceptionMapper implements ExceptionMapper<SerializationFormatValidationException> {
    @Override
    public Response toResponse(SerializationFormatValidationException exception) {
        return Response.status(400).entity("Item in serialization format is not canonicalized: '" + exception.getMessage() + "'").build();
    }
}