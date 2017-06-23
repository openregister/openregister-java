package uk.gov.register.exceptions;

import uk.gov.register.core.RegisterName;

public class FieldUndefinedException extends RuntimeException {
    public FieldUndefinedException(RegisterName registerName, String fieldName) {
        super(String.format("Field undefined: %s - %s", registerName.value(), fieldName));
    }
}
