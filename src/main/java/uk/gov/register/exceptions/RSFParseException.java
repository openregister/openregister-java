package uk.gov.register.exceptions;

public class RSFParseException extends RuntimeException {
    public RSFParseException(String message) {
        super(message);
    }

    public RSFParseException(String message, Exception e){
        super(message, e);
    }
}
