package uk.gov.register.exceptions;

import uk.gov.register.util.HashValue;

/**
 * Used when an item with a particular item hash does not exist in the register
 */
public class NoSuchItemException extends RuntimeException{

    public NoSuchItemException(HashValue hashValue) {
        this(String.format("No item %s found", hashValue.toString()));
    }

    public NoSuchItemException(String message) {
        super(message);
    }

    public NoSuchItemException(String message, Throwable cause) {
        super(message, cause);
    }
}
