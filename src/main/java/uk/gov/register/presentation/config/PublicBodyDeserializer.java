package uk.gov.register.presentation.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.IOException;

public class PublicBodyDeserializer extends JsonDeserializer<PublicBody> {
    private PublicBodiesConfiguration configuration;

    public PublicBodyDeserializer(PublicBodiesConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public PublicBody deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        TreeNode treeNode = p.readValueAsTree();
        String publicBodyName = ((TextNode) treeNode).asText();
        return configuration.getPublicBody(publicBodyName);
    }
}
