package uk.gov.register.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicBody {
    private final String name;
    private final String publicBodyId;

    @JsonCreator
    public PublicBody(@JsonProperty("name") String name, @JsonProperty("public-body") String publicBodyId) {
        this.name = name;
        this.publicBodyId = publicBodyId;
    }

    public String getName() {
        return name;
    }

    public String getPublicBodyId() {
        return publicBodyId;
    }
}
