package uk.gov.register.exceptions;

import uk.gov.register.core.RegisterId;

import javax.ws.rs.NotFoundException;

/**
 * Used if the definition of a particular field is missing from the register
 */
public class NoSuchFieldException extends NotFoundException {
    public NoSuchFieldException(RegisterId registerId, String fieldName) {
        super(String.format("Field undefined: %s - %s", registerId.value(), fieldName));
    }
}
