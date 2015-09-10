package uk.gov.register.presentation;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.register.presentation.representations.FieldValueJsonSerializer;

@JsonSerialize(using = FieldValueJsonSerializer.class)
public class LinkValue implements FieldValue {
    private static final String template = "http://%1$s.openregister.org/%1$s/%2$s";
    private final String value;
    private final String link;


    public LinkValue(String name, String value) {
        this.value = value;
        this.link = String.format(template, name, value);
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
