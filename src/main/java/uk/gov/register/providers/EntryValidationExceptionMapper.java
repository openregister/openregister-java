package uk.gov.register.providers;

import uk.gov.register.exceptions.EntryValidationException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class EntryValidationExceptionMapper implements ExceptionMapper<EntryValidationException> {
	@Override
	public Response toResponse(EntryValidationException exception) {
		return Response.status(400).entity("Entry validation exception: " + exception.getMessage()).build();
	}
}
