package uk.gov.register.providers;

import uk.gov.register.exceptions.RegisterUndefinedException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RegisterUndefinedExceptionMapper implements ExceptionMapper<RegisterUndefinedException> {

    @Override
    public Response toResponse(RegisterUndefinedException exception) {
        return Response
                .status(Response.Status.OK)
                .entity("Register undefined")
                .build();
    }
}
