package uk.gov.register.views;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.json.JSONException;
import org.junit.Test;
import uk.gov.register.core.RegisterData;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class RecordViewTest {
    @Test
    public void recordJsonRepresentation_isFlatJsonOfEntryAndItemContent() throws IOException, JSONException {
        ObjectMapper objectMapper = Jackson.newObjectMapper();

        Record record = new Record(new Entry("1", "ab", Instant.ofEpochMilli(1459241964336L)), new Item("ab", objectMapper.readTree("{\"a\":\"b\"}")));
        RecordView recordView = new RecordView(null, null, null, null, record, () -> "test.register.gov.uk", new RegisterData(Collections.emptyMap()));

        String result = objectMapper.writeValueAsString(recordView);

        assertThat(result, equalTo("{" +
                "\"entry-number\":\"1\"," +
                "\"entry-timestamp\":\"2016-03-29T08:59:24Z\"," +
                "\"item-hash\":\"sha-256:ab\"," +
                "\"a\":\"b\"" +
                "}"));
    }
}
