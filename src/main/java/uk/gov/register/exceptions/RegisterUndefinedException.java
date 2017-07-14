package uk.gov.register.exceptions;

import uk.gov.register.core.RegisterName;

public class RegisterUndefinedException extends RuntimeException {
    public RegisterUndefinedException(RegisterName registerName) {
        super("Register undefined for " + registerName.value());
    }
}
