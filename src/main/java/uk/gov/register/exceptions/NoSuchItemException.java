package uk.gov.register.exceptions;

public class NoSuchItemException extends RuntimeException {
    private final String sha256hex;

    public NoSuchItemException(String sha256hex) {
        super("No item found with sha256hex " + sha256hex);
        this.sha256hex = sha256hex;
    }

    public String getSha256Hex() {
        return sha256hex;
    }
}
