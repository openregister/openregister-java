package uk.gov.mint;

public class CTException extends RuntimeException {
    private int status;

    public CTException(int status, String reason) {
        super(reason);

        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
