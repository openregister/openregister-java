package uk.gov.register.views;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.json.JSONException;
import org.junit.Test;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.core.RegisterData;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class RecordViewTest {
    @Test
    public void recordJsonRepresentation_isFlatJsonOfEntryAndItemContent() throws IOException, JSONException {
        ObjectMapper objectMapper = Jackson.newObjectMapper();

        Record record = new Record(new Entry(1, "ab", Instant.ofEpochSecond(1470403440)), new Item("ab", objectMapper.readTree("{\"a\":\"b\"}")));
        RecordView recordView = new RecordView(null, null, null, null, record, () -> "test.register.gov.uk", new RegisterData(Collections.emptyMap()));

        String result = objectMapper.writeValueAsString(recordView);

        assertThat(result, equalTo("{" +
                "\"entry-number\":\"1\"," +
                "\"entry-timestamp\":\"2016-08-05T13:24:00Z\"," +
                "\"item-hash\":\"sha-256:ab\"," +
                "\"a\":\"b\"" +
                "}"));
    }
}
