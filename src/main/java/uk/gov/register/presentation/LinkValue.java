package uk.gov.register.presentation;

public class LinkValue implements FieldValue {
    private static final String template = "http://%1$s.openregister.org/%2$s/%3$s";
    private final String value;
    private final String link;

    public LinkValue(String registerName, String primaryKey, String value) {
        this.value = value;
        this.link = String.format(template, registerName, primaryKey, value);
    }

    @Override
    public boolean isLink() {
        return true;
    }

    @Override
    public String value() {
        return value;
    }

    public String link() {
        return link;
    }
}
