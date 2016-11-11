package uk.gov.register.providers;

import uk.gov.register.exceptions.SerializedRegisterParseException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class SerializedRegisterParseExceptionMapper implements ExceptionMapper<SerializedRegisterParseException> {

    @Override
    public Response toResponse(SerializedRegisterParseException exception) {
        return Response.status(400).entity("Error parsing : "  + exception.getMessage()).build();
    }
}
