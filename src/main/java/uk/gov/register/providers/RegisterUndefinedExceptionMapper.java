package uk.gov.register.providers;

import uk.gov.register.exceptions.NoSuchRegisterException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RegisterUndefinedExceptionMapper implements ExceptionMapper<NoSuchRegisterException> {

    @Override
    public Response toResponse(NoSuchRegisterException exception) {
        return Response
                .status(Response.Status.OK)
                .entity("Register undefined")
                .build();
    }
}
