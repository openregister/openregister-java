package uk.gov.register.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.register.core.datatype.Datatype;

import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Field {
    final FieldItem item;

    public Field(FieldItem item) {
        this.item = item;
    }

    @JsonCreator
    public Field(@JsonProperty("item") List<FieldItem> items) {
        this.item = items.get(0);
    }

    public Optional<RegisterName> getRegister() {
        return item.register;
    }

    @SuppressWarnings("unused")
    public Cardinality getCardinality() {
        return item.cardinality;
    }

    public Datatype getDatatype() {
        return item.datatype;
    }

    public String getText() {
        return item.text;
    }

    public String getFieldName() { return item.fieldName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Field field = (Field) o;

        return item != null ? item.equals(field.item) : field.item == null;
    }

    @Override
    public int hashCode() {
        return item != null ? item.hashCode() : 0;
    }
}