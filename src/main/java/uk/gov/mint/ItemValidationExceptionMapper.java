package uk.gov.mint;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ItemValidationExceptionMapper implements ExceptionMapper<ItemValidationException> {

    @Override
    public Response toResponse(ItemValidationException exception) {
        return Response.status(400).entity(exception.getMessage() + ". Error entry: '" + exception.getEntry().toString() + "'").build();
    }
}
