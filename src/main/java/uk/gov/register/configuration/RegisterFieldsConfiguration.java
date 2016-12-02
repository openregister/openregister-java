package uk.gov.register.configuration;

import com.google.common.collect.Lists;
import uk.gov.register.core.RegisterData;

import javax.inject.Inject;
import java.util.ArrayList;

public class RegisterFieldsConfiguration {
    private final ArrayList<String> registerFields;

    @Inject
    public RegisterFieldsConfiguration(RegisterData registerData) {
        this.registerFields = Lists.newArrayList(registerData.getRegister().getFields());
    }

    public boolean containsField(String fieldName) {
        return registerFields.contains(fieldName);
    }
}
