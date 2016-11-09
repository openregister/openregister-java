package uk.gov.register.exceptions;

import javax.ws.rs.NotFoundException;

public class NoSuchFieldException extends NotFoundException {
    public NoSuchFieldException(String registerName, String fieldName) {
        super("No field found matching " + fieldName + " in register " + registerName);
    }
}