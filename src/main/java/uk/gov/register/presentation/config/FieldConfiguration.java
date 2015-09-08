package uk.gov.register.presentation.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import javax.validation.Valid;
import java.util.Optional;

public class FieldConfiguration {
    @Valid
    private Optional<String> register;

    @JsonCreator
    public FieldConfiguration(
            @JsonProperty("field") String field,
            @JsonProperty("datatype") String datatype,
            @JsonProperty("register") String register,
            @JsonProperty("cardinality") String cardinality,
            @JsonProperty("text") String text) {
        this.register = StringUtils.isEmpty(register) ? Optional.empty() : Optional.of(register);
    }

    public Optional<String> getRegister() {
        return register;
    }
}
