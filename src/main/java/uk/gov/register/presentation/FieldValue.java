package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface FieldValue {
    String getValue();

    @JsonIgnore
    boolean isLink();

    @JsonIgnore
    boolean isList();
}
