package uk.gov.register.presentation;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.register.presentation.representations.FieldValueJsonSerializer;

@JsonSerialize(using = FieldValueJsonSerializer.class)
public class StringValue implements FieldValue {
    String value;


    public StringValue(String value) {
        this.value = value;
    }

    @Override
    public boolean isLink() {
        return false;
    }

    @Override
    public String value() {
        return value;
    }
}
