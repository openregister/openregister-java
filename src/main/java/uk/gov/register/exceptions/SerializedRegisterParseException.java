package uk.gov.register.exceptions;

public class SerializedRegisterParseException extends RuntimeException {

    public SerializedRegisterParseException(String message) {
        super(message);
    }

    public SerializedRegisterParseException(String message, Exception e) {
        super(message, e);
    }

}
