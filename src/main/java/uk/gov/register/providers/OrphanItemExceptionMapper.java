package uk.gov.register.providers;

import uk.gov.register.exceptions.NoSuchItemForEntryException;
import uk.gov.register.exceptions.OrphanItemException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class OrphanItemExceptionMapper implements ExceptionMapper<OrphanItemException> {

    @Override
    public Response toResponse(OrphanItemException ex) {
        return Response.status(400).type(MediaType.APPLICATION_JSON_TYPE).entity(ex.getErrorJson()).build();
    }

}
