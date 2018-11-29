package uk.gov.register.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.objecthash.ObjectHash;

import java.io.IOException;

import static org.junit.Assert.*;

public class JsonToBlobHashTest {

    @Test
    public void hashesJsonNodeWithCardinality1() throws IOException {
        JsonNode jsonNode = new ObjectMapper().readTree("{\"foo\": \"bar\"}");

        assertEquals("7ef5237c3027d6c58100afadf37796b3d351025cf28038280147d42fdc53b960", JsonToBlobHash.apply(jsonNode).getValue());
    }

    @Test
    public void hashesJsonNodeWithCardinalityN() throws IOException {
        JsonNode jsonNode = new ObjectMapper().readTree("{\"foo\": [\"bar\", \"baz\"]}");

        assertEquals("e64c38241421a64d3cbe3e0f1a7b1a429bd7b9b5ce3bb4d0840f70ecee3cd460", JsonToBlobHash.apply(jsonNode).getValue());
    }

}
