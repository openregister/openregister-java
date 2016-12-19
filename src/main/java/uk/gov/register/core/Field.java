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
}
