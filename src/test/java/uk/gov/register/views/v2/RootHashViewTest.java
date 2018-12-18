package uk.gov.register.views.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.util.HashValue;

import static org.junit.Assert.assertEquals;

public class RootHashViewTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @Test
    public void jsonResponse() throws JsonProcessingException {
        RootHashView view = new RootHashView(new HashValue(HashingAlgorithm.SHA256, "8d92e1e0af1d43c41e498e6baed0d0b3ea2770d1bf9d2afc04e9c4dad7795729"), 208);
        String result = objectMapper.writeValueAsString(view);
        assertEquals(
            result,
    "{" +
            "\"root-hash\":\"12208d92e1e0af1d43c41e498e6baed0d0b3ea2770d1bf9d2afc04e9c4dad7795729\"," +
            "\"total-entries\":208" +
            "}"
        );
    }
}
