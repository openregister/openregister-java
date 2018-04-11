package uk.gov.register.exceptions;

import uk.gov.register.core.RegisterId;

import javax.ws.rs.NotFoundException;

public class NoSuchFieldException extends NotFoundException {
    public NoSuchFieldException(RegisterId registerId, String fieldName) {
        super("No field found matching " + fieldName + " in register " + registerId);
    }
}
