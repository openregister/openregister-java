package uk.gov.register.presentation.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.Set;

public class Register {
    final String registerName;
    final Set<String> fields;
    final Optional<String> copyright;
    final String registry;
    final String text;

    @JsonCreator
    public Register(@JsonProperty("register") String registerName,
                    @JsonProperty("fields") Set<String> fields,
                    @JsonProperty("copyright") String copyright,
                    @JsonProperty("registry") String registry,
                    @JsonProperty("text") String text) {
        this.registerName = registerName;
        this.fields = fields;
        this.copyright = StringUtils.isNotEmpty(copyright) ? Optional.of(copyright) : Optional.empty();
        this.registry = registry;
        this.text = text;
    }

    public Optional<String> getCopyright() {
        return copyright;
    }

    public Set<String> getFields() {
        return fields;
    }
}
