package uk.gov.register.exceptions;

import uk.gov.register.core.RegisterId;

public class FieldUndefinedException extends RuntimeException {
    public FieldUndefinedException(RegisterId registerId, String fieldName) {
        super(String.format("Field undefined: %s - %s", registerId.value(), fieldName));
    }
}
