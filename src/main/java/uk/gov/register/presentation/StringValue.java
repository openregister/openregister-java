package uk.gov.register.presentation;

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
