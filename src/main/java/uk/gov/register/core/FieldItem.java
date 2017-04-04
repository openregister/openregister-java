package uk.gov.register.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.register.core.datatype.Datatype;
import uk.gov.register.core.datatype.DatatypeFactory;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldItem {
    public final String fieldName;
    final Datatype datatype;
    final Optional<RegisterName> register;
    final Cardinality cardinality;
    final String text;

    @JsonCreator
    public FieldItem(
                @JsonProperty("field") String fieldName,
                @JsonProperty("datatype") String datatype,
                @JsonProperty("register") RegisterName register,
                @JsonProperty("cardinality") Cardinality cardinality,
                @JsonProperty("text") String text) {
        this.fieldName = fieldName;
        this.text = text;
        this.register = Optional.ofNullable(register);
        this.cardinality = cardinality;
        this.datatype = DatatypeFactory.get(datatype);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldItem item = (FieldItem) o;

        if (fieldName != null ? !fieldName.equals(item.fieldName) : item.fieldName != null) return false;
        if (datatype != null ? !datatype.equals(item.datatype) : item.datatype != null) return false;
        if (register != null ? !register.equals(item.register) : item.register != null) return false;
        if (cardinality != item.cardinality) return false;
        return text != null ? text.equals(item.text) : item.text == null;
    }

    @Override
    public int hashCode() {
        int result = fieldName != null ? fieldName.hashCode() : 0;
        result = 31 * result + (datatype != null ? datatype.hashCode() : 0);
        result = 31 * result + (register != null ? register.hashCode() : 0);
        result = 31 * result + (cardinality != null ? cardinality.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }

    public Optional<RegisterName> getRegister() {
        return register;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public Datatype getDatatype() {
        return datatype;
    }

    public String getText() {
        return text;
    }
}
