package uk.gov.register.presentation.representations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterators;
import com.google.common.reflect.TypeToken;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Produces(ExtraMediaType.TEXT_TSV)
public class TsvWriter implements MessageBodyWriter<List<JsonNode>> {
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        Type listOfJsonNode = new TypeToken<List<JsonNode>>() {}.getType();
        return genericType.equals(listOfJsonNode);
    }

    @Override
    public long getSize(List<JsonNode> jsonNodes, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        // deprecated and ignored by Jersey 2. Returning -1 as per javadoc in the interface
        return -1;
    }

    @Override
    public void writeTo(List<JsonNode> jsonNodes, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        List<String> headers = getHeaders(jsonNodes.get(0));
        entityStream.write((String.join("\t", headers) + "\n").getBytes("utf-8"));
        for (JsonNode jsonNode : jsonNodes) {
            writeRow(entityStream, headers, jsonNode);
        }
    }

    private void writeRow(OutputStream entityStream, List<String> headers, JsonNode node) throws IOException {
        ObjectNode entry = (ObjectNode) node.get("entry");
        entry.set("hash", node.get("hash"));
        String row = headers.stream().map(e -> entry.get(e).textValue()).collect(Collectors.joining("\t", "", "\n"));
        entityStream.write(row.getBytes("utf-8"));
    }

    private List<String> getHeaders(JsonNode jsonNode) {
        List<String> headers = new ArrayList<>();
        headers.add("hash");
        Iterators.addAll(headers, jsonNode.get("entry").fieldNames());
        return headers;
    }
}
