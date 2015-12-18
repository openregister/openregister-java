package uk.gov.mint;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class EntryValidationExceptionMapper implements ExceptionMapper<EntryValidationException> {

    @Override
    public Response toResponse(EntryValidationException exception) {
        return Response.status(400).entity(exception.getMessage() + " Error entry: '" + exception.getEntry().toString() + "'").build();
    }
}
