package uk.gov.register.configuration;

import com.google.common.collect.Lists;

import java.util.ArrayList;

public class RegisterFieldsConfiguration {
    private final ArrayList<String> registerFields;

    public RegisterFieldsConfiguration(Iterable<String> fields) {
        this.registerFields = Lists.newArrayList(fields);
    }

    public boolean containsField(String fieldName) {
        return registerFields.contains(fieldName);
    }
}
