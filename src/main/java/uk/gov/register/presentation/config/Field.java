package uk.gov.register.presentation.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class Field {
    String fieldName;
    String datatype;
    Optional<String> register;
    String cardinality;
    String text;

    @JsonCreator
    public Field(@JsonProperty("field") String fieldName,
                 @JsonProperty("datatype") String datatype,
                 @JsonProperty("register") String register,
                 @JsonProperty("cardinality") String cardinality,
                 @JsonProperty("text") String text) {
        this.fieldName = fieldName;
        this.datatype = datatype;
        this.register = StringUtils.isNotEmpty(register) ? Optional.of(register) : Optional.empty();
        this.cardinality = cardinality;
        this.text = text;
    }

    public Optional<String> getRegister() {
        return register;
    }
}
