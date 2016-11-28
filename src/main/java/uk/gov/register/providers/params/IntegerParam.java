package uk.gov.register.providers.params;

import io.dropwizard.jersey.params.IntParam;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;

public class IntegerParam extends IntParam {
    public IntegerParam(String input) {
        super(input);
    }

    @Override
    protected Response error(String input, Exception e) {
        throw new BadRequestException(errorMessage(e));
    }
}
