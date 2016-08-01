package uk.gov.register.core;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.register.core.FieldValue;

import javax.validation.constraints.NotNull;

public class StringValue implements FieldValue {
    @NotNull
    public final String value;

    public StringValue(@NotNull String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    public boolean isLink() {
        return false;
    }

    public boolean isList() {
        return false;
    }
}
