package uk.gov.register.exceptions;

/**
 * Used if a problem occurs consuming RSF
 */
public class RSFParseException extends RuntimeException {
    public RSFParseException(String message) {
        super(message);
    }

    public RSFParseException(String message, Exception e){
        super(message, e);
    }
}
