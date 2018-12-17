package uk.gov.register.views.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.util.HashValue;

import java.util.Arrays;

import static org.junit.Assert.*;

public class AuditPathViewTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @Test
    public void jsonRepresentation() throws JsonProcessingException {
        AuditPathView view = new AuditPathView(Arrays.asList(
                new HashValue(HashingAlgorithm.SHA256, "f0ebeef6be205cfc5fb6b4a314294bdff471f5409594f742b0f30c8551278b4a"),
                new HashValue(HashingAlgorithm.SHA256, "8dc980062c4e6ffd2300b72cd5a6a67e23070aabec31911691c657c2e1dd37a6"),
                new HashValue(HashingAlgorithm.SHA256, "c48916df15f3f6e030d84bf0f8bb59460c472250d38db27b4cd2e7394fe0741d")
        ));

        String result = objectMapper.writeValueAsString(view);
        assertEquals(
                result,
                "{" +
                        "\"audit-path\":[" +
                        "\"1220f0ebeef6be205cfc5fb6b4a314294bdff471f5409594f742b0f30c8551278b4a\"," +
                        "\"12208dc980062c4e6ffd2300b72cd5a6a67e23070aabec31911691c657c2e1dd37a6\"," +
                        "\"1220c48916df15f3f6e030d84bf0f8bb59460c472250d38db27b4cd2e7394fe0741d\"" +
                        "]" +
                        "}"
        );
    }
}
