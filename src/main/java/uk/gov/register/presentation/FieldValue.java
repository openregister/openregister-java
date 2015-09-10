package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

public interface FieldValue {
    @JsonIgnore
    boolean isLink();

    @JsonValue
    String value();
}
