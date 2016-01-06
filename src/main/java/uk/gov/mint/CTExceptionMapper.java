package uk.gov.mint;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class CTExceptionMapper implements ExceptionMapper<CTException> {
    public Response toResponse(CTException ex) {
        return Response.status(ex.getStatus()).entity(ex.getMessage()).type("text/plain").build();
    }
}
