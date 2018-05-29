package uk.gov.register.providers;

import uk.gov.register.exceptions.IndexingException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class EntryValidationExceptionMapper implements ExceptionMapper<IndexingException> {
	@Override
	public Response toResponse(IndexingException exception) {
		return Response.status(400).entity("Entry validation exception: " + exception.getMessage()).build();
	}
}
