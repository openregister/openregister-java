package uk.gov.register;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

@JsonIgnoreProperties({"phase", "text", "registry", "copyright"})
public class Register {
    final String registerName;
    final Set<String> fields;

    @JsonCreator
    public Register(@JsonProperty("register") String registerName,
                    @JsonProperty("fields") Set<String> fields) {
        this.registerName = registerName;
        this.fields = fields;
    }

    public String getRegisterName() {
        return registerName;
    }

    public Iterable<String> getFields() {
        return fields;
    }
}
