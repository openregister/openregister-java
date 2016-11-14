package uk.gov.register.exceptions;

public class SerializationFormatValidationException extends RuntimeException {
    public SerializationFormatValidationException(String jsonString) {
        super(jsonString);
    }
}