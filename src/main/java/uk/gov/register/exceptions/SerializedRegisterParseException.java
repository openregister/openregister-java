package uk.gov.register.exceptions;

import com.fasterxml.jackson.databind.JsonNode;

public class SerializedRegisterParseException extends RuntimeException {

    public SerializedRegisterParseException(String message) {
        super(message);
    }

    public SerializedRegisterParseException(String message, Exception e) {
        super(message, e);
    }

}
