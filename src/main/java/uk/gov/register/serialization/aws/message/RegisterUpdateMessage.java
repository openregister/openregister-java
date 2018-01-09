package uk.gov.register.serialization.aws.message;

public class RegisterUpdateMessage {

    private String registerName;
    private String payload;

    public RegisterUpdateMessage(final String registerName, final String payload) {
        this.registerName = registerName;
        this.payload = payload;
    }

    public String getRegisterName() {
        return registerName;
    }

    public RegisterUpdateMessage setRegisterName(final String registerName) {
        this.registerName = registerName;
        return this;
    }

    public String getPayload() {
        return payload;
    }

    public RegisterUpdateMessage setPayload(final String payload) {
        this.payload = payload;
        return this;
    }

    public String toJson() {
        return "{"
                + "\"registerName\":" + "\"" + registerName + "\","
                + "\"payload\":" + "\"" + payload + "\""
                + "}";
    }
}
