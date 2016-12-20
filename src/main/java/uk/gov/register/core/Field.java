package uk.gov.register.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.register.core.datatype.Datatype;
import uk.gov.register.core.datatype.DatatypeFactory;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Field {
    public final String fieldName;
    final Datatype datatype;
    final Optional<RegisterName> register;
    final Cardinality cardinality;
    final String text;

    @JsonCreator
    public Field(@JsonProperty("field") String fieldName,
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

    public Optional<RegisterName> getRegister() {
        return register;
    }

    @SuppressWarnings("unused")
    public Cardinality getCardinality() {
        return cardinality;
    }

    public Datatype getDatatype() {
        return datatype;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Field field = (Field) o;

        if (fieldName != null ? !fieldName.equals(field.fieldName) : field.fieldName != null) return false;
        if (datatype != null ? !datatype.equals(field.datatype) : field.datatype != null) return false;
        if (register != null ? !register.equals(field.register) : field.register != null) return false;
        if (cardinality != field.cardinality) return false;
        return text != null ? text.equals(field.text) : field.text == null;
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
}
