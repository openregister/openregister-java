package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonValue;

import javax.validation.constraints.NotNull;

public class StringValue implements FieldValue {
    @NotNull
    public final String value;

    public StringValue(@NotNull String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StringValue that = (StringValue) o;

        return value.equals(that.value);

    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
