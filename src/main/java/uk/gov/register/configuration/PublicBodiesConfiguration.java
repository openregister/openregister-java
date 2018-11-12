package uk.gov.register.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.register.core.PublicBody;
import uk.gov.register.util.ResourceJsonFileReader;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class PublicBodiesConfiguration {

    private final Collection<PublicBody> publicBodies;

    public PublicBodiesConfiguration(Optional<String> publicBodiesResourceYamlPath) {
        publicBodies = new ResourceJsonFileReader().readResource(
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
