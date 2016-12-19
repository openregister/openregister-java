package uk.gov.register.exceptions;

import uk.gov.register.core.RegisterName;

import javax.ws.rs.NotFoundException;

public class NoSuchFieldException extends NotFoundException {
    public NoSuchFieldException(RegisterName registerName, String fieldName) {
        super("No field found matching " + fieldName + " in register " + registerName);
    }
}
