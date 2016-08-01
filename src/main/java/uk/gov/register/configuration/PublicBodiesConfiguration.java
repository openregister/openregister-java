package uk.gov.register.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.register.util.ResourceYamlFileReader;

import java.util.*;

public class PublicBodiesConfiguration {

    private final Collection<PublicBody> publicBodies;

    public PublicBodiesConfiguration(Optional<String> publicBodiesResourceYamlPath) {
        publicBodies = new ResourceYamlFileReader().readResource(
                publicBodiesResourceYamlPath,
                "config/public-bodies.yaml",
                new TypeReference<Map<String, PublicBody>>() {
                }
        );
    }

    public PublicBody getPublicBody(String publicBodyId) {
        return publicBodies.stream().filter(f -> Objects.equals(f.getPublicBodyId(), publicBodyId)).findFirst().get();
    }
}
