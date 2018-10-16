package uk.gov.register.exceptions;

import uk.gov.register.util.HashValue;

/**
 * Used when a blob with a particular item hash does not exist in the register
 */
public class NoSuchBlobException extends RuntimeException{

    public NoSuchBlobException(HashValue hashValue) {
        this(String.format("No blob %s found", hashValue.toString()));
    }

    public NoSuchBlobException(String message) {
        super(message);
    }

    public NoSuchBlobException(String message, Throwable cause) {
        super(message, cause);
    }
}
