package uk.gov.register.exceptions;

import uk.gov.register.core.RegisterId;

public class RegisterUndefinedException extends RuntimeException {
    public RegisterUndefinedException(RegisterId registerId) {
        super("Register undefined for " + registerId.value());
    }
}
