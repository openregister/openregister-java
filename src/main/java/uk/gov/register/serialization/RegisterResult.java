package uk.gov.register.serialization;

import com.fasterxml.jackson.annotation.*;

import java.util.Optional;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"success", "message", "details"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterResult {
    private Boolean isSuccessful;
    private String message;
    private Optional<Exception> exception;

    private RegisterResult(Boolean isSuccessful, String message, Optional<Exception> exception) {
        this.isSuccessful = isSuccessful;
        this.message = message;
        this.exception = exception;
    }

    public static RegisterResult createSuccessResult() {
        return new RegisterResult(true, "success", Optional.empty());
    }

    public static RegisterResult createFailResult(String message) {
        return new RegisterResult(false, message, Optional.empty());
    }

    public static RegisterResult createFailResult(String message, Exception exception) {
        return new RegisterResult(false, message, Optional.of(exception));
    }

    @JsonProperty("success")
    public Boolean isSuccessful() {
        return isSuccessful;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("details")
    public String getDetails() {
        return exception.map(this::getCauseMessage).orElse(null);
    }

    @JsonIgnore
    public Optional<Exception> getException() {
        return exception;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegisterResult)) return false;

        RegisterResult registerResult = (RegisterResult) o;

        if (!isSuccessful.equals(registerResult.isSuccessful)) return false;
        if (!message.equals(registerResult.message)) return false;
        return exception.equals(registerResult.exception);

    }

    @Override
    public int hashCode() {
        int result = isSuccessful.hashCode();
        result = 31 * result + message.hashCode();
        result = 31 * result + exception.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "RegisterResult{" +
                "isSuccessful=" + isSuccessful +
                ", message='" + message + '\'' +
                ", exception=" + exception +
                '}';
    }

    private String getCauseMessage(Throwable exception) {
        Throwable cause = null;
        Throwable result = exception;
        String message = exception.getMessage();

        while(null != (cause = result.getCause()) && (result != cause) ) {
            result = cause;
            message += ": " + cause.getMessage();
        }
        return message;
    }
}
