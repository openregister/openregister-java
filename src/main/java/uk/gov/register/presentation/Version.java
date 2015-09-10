package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Version {
    @JsonProperty
    public final String hash;

    public Version(String hash) {
        this.hash = hash;
    }
}
