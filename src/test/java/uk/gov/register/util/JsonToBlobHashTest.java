package uk.gov.register.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.objecthash.ObjectHash;

import java.io.IOException;

import static org.junit.Assert.*;

public class JsonToBlobHashTest {

    @Test
    public void hashesJsonNode() throws IOException {
        JsonNode jsonNode = new ObjectMapper().readTree("{\"foo\": \"bar\"}");

        assertEquals("7ef5237c3027d6c58100afadf37796b3d351025cf28038280147d42fdc53b960", JsonToBlobHash.apply(jsonNode).getValue());
    }

}