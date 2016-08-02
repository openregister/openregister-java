package uk.gov.register.providers;

import uk.gov.register.exceptions.ItemValidationException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ItemValidationExceptionMapper implements ExceptionMapper<ItemValidationException> {

    @Override
    public Response toResponse(ItemValidationException exception) {
        return Response.status(400).entity(exception.getMessage() + ". Error entry: '" + exception.getEntry().toString() + "'").build();
    }
}
