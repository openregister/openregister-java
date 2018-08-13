package uk.gov.register.core;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * A value representing a link. Its "value" is a string representation. It also knows
 * its target register, and its primary key within the target register.
 *
 * For a regular RegisterLinkValue, the "value" and "linkKey" are the same. For a CurieValue, the "value"
 * is the Curie as a string, while the "linkKey" is the second half of the Curie (after the colon).
 */
public class RegisterLinkValue extends LinkValue implements FieldValue {
    private final RegisterId targetRegister;
    private final String value;
    private final String linkKey;

    public RegisterLinkValue(RegisterId registerId, String value) {
        this(registerId, value, value);
    }

    private RegisterLinkValue(RegisterId registerId, String value, String linkKey){
        this.targetRegister = registerId;
        this.value = value;
        this.linkKey = linkKey;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public boolean isLinkToRegister() {
        return true;
    }

    public RegisterId getTargetRegister() {
        return targetRegister;
    }

    public String getLinkKey() {
        return linkKey;
    }

    public static class CurieValue extends RegisterLinkValue {
        public CurieValue(String curieValue) {
            super(new RegisterId(curieValue.split(":")[0]), curieValue, curieValue.split(":")[1]);
        }
    }
}
