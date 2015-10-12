package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface FieldValue {
    @JsonIgnore
    default boolean isLink() {
        return false;
    }

    String value();
}
