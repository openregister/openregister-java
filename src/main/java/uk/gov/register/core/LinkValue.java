package uk.gov.register.core;

import com.fasterxml.jackson.annotation.JsonValue;

public class LinkValue implements FieldValue {
    private static final String template = "%1$s://%2$s.%3$s/record/%4$s";
    private final String targetRegister;
    private final String value;
    private final String link;
    private final String linkKey;

    public LinkValue(String registerName, String registerDomain, String requestScheme, String value) {
        this(registerName, registerDomain, requestScheme, value, value);
    }

    private LinkValue(String registerName, String registerDomain, String requestScheme, String value, String linkKey){
        this.targetRegister = registerName;
        this.value = value;
        this.link = String.format(template, requestScheme, registerName, registerDomain, linkKey);
        this.linkKey = linkKey;
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

    public String getTargetRegister() {
        return targetRegister;
    }

    public String getLinkKey() {
        return linkKey;
    }

    public static class CurieValue extends LinkValue {
        public CurieValue(String curieValue, String registerDomain, String requestScheme) {
            super(curieValue.split(":")[0], registerDomain, requestScheme, curieValue, curieValue.split(":")[1]);
        }
    }
}
