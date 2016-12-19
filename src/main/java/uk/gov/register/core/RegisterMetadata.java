package uk.gov.register.core;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;

@JsonInclude(NON_NULL)
public class RegisterMetadata {
    @JsonProperty("register")
    private RegisterName registerName;
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

    public RegisterMetadata(RegisterName registerName,
                            List<String> fields,
                            String copyright,
                            String registry,
                            String text,
                            String phase) {
        this.registerName = registerName;
        this.phase = phase;
        this.fields = fields;
        this.copyright = StringUtils.isNotEmpty(copyright) ? copyright : null;
        this.registry = registry;
        this.text = text;
    }

    public RegisterName getRegisterName() {
        return registerName;
    }

    public String getCopyright() {
        return copyright;
    }

    @JsonIgnore
    public Iterable<String> getNonPrimaryFields() {
        return Iterables.filter(fields, not(equalTo(registerName.value())));
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
