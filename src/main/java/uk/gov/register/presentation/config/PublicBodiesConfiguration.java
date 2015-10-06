package uk.gov.register.presentation.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;
import io.dropwizard.jackson.Jackson;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class PublicBodiesConfiguration {

    private final List<PublicBody> publicBodies;

    public PublicBodiesConfiguration() throws IOException {
        InputStream publicBodiesStream = this.getClass().getClassLoader().getResourceAsStream("config/public-bodies.yaml");
        ObjectMapper yamlObjectMapper = Jackson.newObjectMapper(new YAMLFactory());
        List<PublicBodyData> rawPublicBodies = yamlObjectMapper.readValue(publicBodiesStream, new TypeReference<List<PublicBodyData>>() {
        });
        publicBodies = Lists.transform(rawPublicBodies, m -> m.entry);
    }

    public PublicBody getPublicBody(String publicBodyId) {
        return publicBodies.stream().filter(f -> Objects.equals(f.getPublicBodyId(), publicBodyId)).findFirst().get();
    }

    @JsonIgnoreProperties({"hash", "last-updated"})
    private static class PublicBodyData{
        @JsonProperty
        PublicBody entry;
    }
}
