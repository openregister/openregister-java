package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonValue;

public class LinkValue implements FieldValue {
    private static final String template = "http://%1$s.openregister.org/%1$s/%2$s";
    private final String value;
    private final String link;

    public LinkValue(String registerName, String value) {
        this.value = value;
        this.link = String.format(template, registerName, value);
    }

    @Override
    public boolean isLink() {
        return true;
    }

    @Override
    @JsonValue
    public String value() {
        return value;
    }

    public String link() {
        return link;
    }
}
