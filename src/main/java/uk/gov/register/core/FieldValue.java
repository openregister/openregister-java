package uk.gov.register.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface FieldValue {
    String getValue();

    @JsonIgnore
    boolean isLink();

    @JsonIgnore
    boolean isList();
}
