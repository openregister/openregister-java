package uk.gov.register.exceptions;

import uk.gov.register.core.RegisterName;

public class RegisterUndefinedException extends RuntimeException {
    public RegisterUndefinedException(RegisterName registerName) {
        super("Register undefined for " + registerName.value());
    }

    public RegisterUndefinedException(RegisterName registerName, String message) {
        super(String.format("Register undefined: %s - %s",registerName.value(), message));
    }
}
