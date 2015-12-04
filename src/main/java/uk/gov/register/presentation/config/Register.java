package uk.gov.register.presentation.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;

public class Register {
    final String registerName;
    final String phase;
    final Set<String> fields;
    final Optional<String> copyright;
    final String registry;
    final String text;

    @JsonCreator
    public Register(@JsonProperty("register") String registerName,
                    @JsonProperty("fields") Set<String> fields,
                    @JsonProperty("copyright") String copyright,
                    @JsonProperty("registry") String registry,
                    @JsonProperty("text") String text,
                    @JsonProperty("phase") String phase) {
        this.registerName = registerName;
        this.phase = phase;
        this.fields = new TreeSet<>(fields); // ensure sorted order
        this.copyright = StringUtils.isNotEmpty(copyright) ? Optional.of(copyright) : Optional.empty();
        this.registry = registry;
        this.text = text;
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

    public String getRegistry() {
        return registry;
    }

    public String getText() {
        return text;
    }
}
