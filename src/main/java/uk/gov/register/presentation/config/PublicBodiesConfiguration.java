package uk.gov.register.presentation.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PublicBodiesConfiguration {

    private final List<PublicBody> publicBodies;

    public PublicBodiesConfiguration(Optional<String> publicBodiesResourceYamlPath) {
        publicBodies = new ResourceYamlFileReader().readResource(
                publicBodiesResourceYamlPath,
                "config/public-bodies.yaml",
                new TypeReference<List<PublicBodyData>>() {
                },
                publicBodyData -> publicBodyData.entry
        );
    }

    public PublicBody getPublicBody(String publicBodyId) {
        return publicBodies.stream().filter(f -> Objects.equals(f.getPublicBodyId(), publicBodyId)).findFirst().get();
    }

    @JsonIgnoreProperties({"hash", "last-updated"})
    private static class PublicBodyData {
        @JsonProperty
        PublicBody entry;
    }
}
