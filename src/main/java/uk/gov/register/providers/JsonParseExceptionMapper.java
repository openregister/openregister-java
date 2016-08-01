package uk.gov.register.providers;

import com.fasterxml.jackson.core.JsonParseException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class JsonParseExceptionMapper implements ExceptionMapper<JsonParseException> {
    @Override
    public Response toResponse(JsonParseException exception) {
        return Response.status(400).entity("Error parsing json input: "  + exception.getMessage()).build();
    }
}
