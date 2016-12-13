package uk.gov.register.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/* dumb wrapper around String to give a type to a register's name */
public class RegisterName {
    private final String name;

    @JsonCreator
    public RegisterName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        this.name = name;
    }

    @JsonValue
    public String value() {
        return name;
    }

    public String getFriendlyRegisterName() {
        return StringUtils.capitalize(name.replace('-', ' '));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegisterName that = (RegisterName) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
