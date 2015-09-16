package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Version {
    @JsonProperty
    public final String hash;
    @JsonProperty("serial-number")
    public final int serialNumber;

    public Version(int serialNumber, String hash) {
        this.hash = hash;
        this.serialNumber = serialNumber;
    }
}
