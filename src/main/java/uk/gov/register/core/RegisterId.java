package uk.gov.register.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/* dumb wrapper around String to give a type to a register's id */
public class RegisterId {
    private final String id;

    @JsonCreator
    public RegisterId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        this.id = id;
    }

    @JsonValue
    public String value() {
        return id;
    }

    public String getFriendlyRegisterName() {
        return StringUtils.capitalize(id.replace('-', ' '));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegisterId that = (RegisterId) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
