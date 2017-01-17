package uk.gov.register.serialization;

import com.fasterxml.jackson.annotation.*;

import java.util.Optional;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"success", "message", "details"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RSFResult {
    private Boolean isSuccessful;
    private String message;
    private Optional<Exception> exception;

    private RSFResult(Boolean isSuccessful, String message, Optional<Exception> exception) {
        this.isSuccessful = isSuccessful;
        this.message = message;
        this.exception = exception;
    }

    public static RSFResult createSuccessResult() {
        return new RSFResult(true, "success", Optional.empty());
    }

    public static RSFResult createFailResult(String message) {
        return new RSFResult(false, message, Optional.empty());
    }

    public static RSFResult createFailResult(String message, Exception exception) {
        return new RSFResult(false, message, Optional.of(exception));
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
        return exception.map(Throwable::getMessage).orElse(null);
    }


    @JsonIgnore
    public Optional<Exception> getException() {
        return exception;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RSFResult)) return false;

        RSFResult rsfResult = (RSFResult) o;

        if (!isSuccessful.equals(rsfResult.isSuccessful)) return false;
        if (!message.equals(rsfResult.message)) return false;
        return exception.equals(rsfResult.exception);

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
        return "RSFResult{" +
                "isSuccessful=" + isSuccessful +
                ", message='" + message + '\'' +
                ", exception=" + exception +
                '}';
    }
}
