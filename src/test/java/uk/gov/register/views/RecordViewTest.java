package uk.gov.register.views;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.json.JSONException;
import org.junit.Test;
import uk.gov.register.core.*;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class RecordViewTest {
    @Test
    public void recordJsonRepresentation_isFlatJsonOfEntryAndItemContent() throws IOException, JSONException {
        ObjectMapper objectMapper = Jackson.newObjectMapper();

        Record record = new Record(new Entry(1, new HashValue(HashingAlgorithm.SHA256, "ab"), Instant.ofEpochSecond(1470403440)), new Item(new HashValue(HashingAlgorithm.SHA256, "ab"), objectMapper.readTree("{\"a\":\"b\"}")));
        RecordView recordView = new RecordView(null, null, null, null, record, () -> "test.register.gov.uk", new RegisterData(Collections.emptyMap()), () -> Optional.empty());

        String result = objectMapper.writeValueAsString(recordView);

        assertThat(result, equalTo("{" +
                "\"entry-number\":\"1\"," +
                "\"entry-timestamp\":\"2016-08-05T13:24:00Z\"," +
                "\"item-hash\":\"sha-256:ab\"," +
                "\"a\":\"b\"" +
                "}"));
    }
}
