package uk.gov.register.core;

import com.fasterxml.jackson.annotation.JsonValue;

import javax.validation.constraints.NotNull;

public class UrlValue extends LinkValue implements FieldValue {
    @NotNull
    public final String value;

    public UrlValue(@NotNull String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public boolean isLinkToRegister() {
        return false;
    }
}
