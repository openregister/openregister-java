package uk.gov.register.core;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;

@JsonInclude(NON_NULL)
public class RegisterMetadata {
    @JsonProperty("register")
    private RegisterId registerId;
    @JsonProperty
    private List<String> fields;
    @JsonProperty
    private String copyright;
    @JsonProperty
    private String registry;
    @JsonProperty
    private String text;
    @JsonProperty
    private String phase;

    private Map<String, JsonNode> otherProperties = new HashMap<>();

    @SuppressWarnings("unused, used by jackson")
    @JsonAnyGetter
    public Map<String, JsonNode> getOtherProperties() {
        return otherProperties;
    }

    @SuppressWarnings("unused, used by jackson")
    @JsonAnySetter
    public void setOtherProperty(String name, JsonNode value) {
        otherProperties.put(name, value);
    }

    @SuppressWarnings("unused, used by jackson")
    public RegisterMetadata() {
    }

    public RegisterMetadata(RegisterId registerId,
                            List<String> fields,
                            String copyright,
                            String registry,
                            String text,
                            String phase) {
        this.registerId = registerId;
        this.phase = phase;
        this.fields = fields;
        this.copyright = StringUtils.isNotEmpty(copyright) ? copyright : null;
        this.registry = registry;
        this.text = text;
    }

    public RegisterId getRegisterId() {
        return registerId;
    }

    public String getCopyright() {
        return copyright;
    }

    @JsonIgnore
    public Iterable<String> getNonPrimaryFields() {
        if (!getPrimaryKeyField().isPresent()) {
            return fields;
        }

        return Iterables.filter(fields, not(equalTo(getPrimaryKeyField().get())));
    }

    @JsonIgnore
    public Optional<String> getPrimaryKeyField() {
        return fields.stream().filter(this::isFieldPrimaryKey).findFirst();
    }

    public List<String> getFields() {
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

    private boolean isFieldPrimaryKey(String field) {
        return field.equals(registerId.value());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegisterMetadata that = (RegisterMetadata) o;

        if (registerId != null ? !registerId.equals(that.registerId) : that.registerId != null) return false;
        if (fields != null ? !fields.equals(that.fields) : that.fields != null) return false;
        if (copyright != null ? !copyright.equals(that.copyright) : that.copyright != null) return false;
        if (registry != null ? !registry.equals(that.registry) : that.registry != null) return false;
        if (text != null ? !text.equals(that.text) : that.text != null) return false;
        if (phase != null ? !phase.equals(that.phase) : that.phase != null) return false;
        return otherProperties != null ? otherProperties.equals(that.otherProperties) : that.otherProperties == null;

    }

    @Override
    public int hashCode() {
        int result = registerId != null ? registerId.hashCode() : 0;
        result = 31 * result + (fields != null ? fields.hashCode() : 0);
        result = 31 * result + (copyright != null ? copyright.hashCode() : 0);
        result = 31 * result + (registry != null ? registry.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (phase != null ? phase.hashCode() : 0);
        result = 31 * result + (otherProperties != null ? otherProperties.hashCode() : 0);
        return result;
    }
}
