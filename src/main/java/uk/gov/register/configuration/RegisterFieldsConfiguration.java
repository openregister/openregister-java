package uk.gov.register.configuration;

import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;

public class RegisterFieldsConfiguration {
    private final ArrayList<String> registerFields;

    public RegisterFieldsConfiguration(Iterable<String> fields) {
        this.registerFields = newArrayList(fields);
    }

    public boolean containsField(String fieldName) {
        return registerFields.contains(fieldName);
    }
}
