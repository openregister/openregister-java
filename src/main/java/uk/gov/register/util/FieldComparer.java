package uk.gov.register.util;

import uk.gov.register.core.Field;

public class FieldComparer {

    public static boolean equals(Field field1, Field field2) {
        return field1.getCardinality().equals(field2.getCardinality())
                && field1.getDatatype().equals(field2.getDatatype())
                && field1.fieldName.equals(field2.fieldName)
                && field1.getRegister().equals(field2.getRegister());
    }
}
