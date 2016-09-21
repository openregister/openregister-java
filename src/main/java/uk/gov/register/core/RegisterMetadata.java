package uk.gov.register.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterMetadata {
    final String registerName;
    final List<String> fields;
    final Optional<String> copyright;
    final String registry;
    final String text;
    final String phase;

    @JsonCreator
    public RegisterMetadata(@JsonProperty("register") String registerName,
                            @JsonProperty("fields") List<String> fields,
                            @JsonProperty("copyright") String copyright,
                            @JsonProperty("registry") String registry,
                            @JsonProperty("text") String text,
                            @JsonProperty("phase") String phase) {
        this.registerName = registerName;
        this.phase = phase;
        this.fields = fields;
        this.copyright = StringUtils.isNotEmpty(copyright) ? Optional.of(copyright) : Optional.empty();
        this.registry = registry;
        this.text = text;
    }

    public String getRegisterName() {
        return registerName;
    }

    public Optional<String> getCopyright() {
        return copyright;
    }

    public Iterable<String> getNonPrimaryFields() {
        return Iterables.filter(fields, not(equalTo(registerName)));
    }

    public Iterable<String> getFields() {
        return fields;
    }

    public String getPhase() {
        return phase;
    }

    public String getRegistry() {
        return registry;
    }

    public String getText() {
        return text;
    }
}
