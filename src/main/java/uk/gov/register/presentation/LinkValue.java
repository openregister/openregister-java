package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonValue;

public class LinkValue implements FieldValue {
    private static final String template = "http://%1$s.openregister.org/%1$s/%2$s";
    private final String value;
    private final String link;

    public LinkValue(String registerName, String value) {
        this(registerName, value, value);
    }

    private LinkValue(String registerName, String value, String linkKey){
        this.value = value;
        this.link = String.format(template, registerName, linkKey);
    }

    @Override
    public boolean isLink() {
        return true;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    public String link() {
        return link;
    }

    public boolean isList() {
        return false;
    }

    public static class CurieValue extends LinkValue {
        public CurieValue(String curieValue) {
            super(curieValue.split(":")[0], curieValue, curieValue.split(":")[1]);
        }
    }
}
