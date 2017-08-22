package uk.gov.register.util;

import uk.gov.register.core.RegisterMetadata;

import java.util.List;
import java.util.Objects;

public class RegisterComparer {

    public static boolean equals(RegisterMetadata registerMetadata1, RegisterMetadata registerMetadata2) {
        return registerMetadata1.getRegisterName().equals(registerMetadata2.getRegisterName())
                && Objects.equals(registerMetadata1.getPhase(), registerMetadata2.getPhase())
                && Objects.equals(registerMetadata1.getRegistry(), registerMetadata2.getRegistry())
                && Objects.equals(registerMetadata1.getCopyright(), registerMetadata2.getCopyright())
                && fieldsMatch(registerMetadata1.getFields(), registerMetadata2.getFields());
    }

    private static boolean fieldsMatch(List<String> fields1, List<String> fields2) {
        return fields1 == null && fields2 == null || (fields1 != null && fields2 != null && fields1.size() == fields2.size() && fields1.containsAll(fields2));
    }
}
