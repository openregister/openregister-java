package uk.gov.register.presentation.representations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.register.presentation.Record;
import uk.gov.register.presentation.view.SingleResultView;

import java.io.StringWriter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class SingleResultViewJsonTest {

    private static ObjectMapper objectMapper;

    @BeforeClass
    public static void setUpOnce() throws Exception {
        objectMapper = Jackson.newObjectMapper();
    }

    @Test
    public void shouldSerializeSingleResultViewToJson() throws Exception {
        Record record = new Record("hash1", objectMapper.readValue(
                "{\"school\":\"100000\",\"name\":\"My School\"}", JsonNode.class));
        StringWriter writer = new StringWriter();
        objectMapper.writeValue(writer, new SingleResultView("don't care", record));
        String result = writer.toString();
        assertThat(objectMapper.readValue(result, JsonNode.class),
                equalTo(objectMapper.readValue("{\"entry\":{\"name\":\"My School\",\"school\":\"100000\"},\"hash\":\"hash1\"}",JsonNode.class)));
    }
}
