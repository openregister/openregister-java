package uk.gov.register.presentation.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.json.JSONException;
import org.junit.Test;
import uk.gov.register.presentation.dao.Entry;
import uk.gov.register.presentation.dao.Item;
import uk.gov.register.presentation.dao.Record;

import java.io.IOException;
import java.time.Instant;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class RecordViewTest {
    @Test
    public void recordJsonRepresentation_isFlatJsonOfEntryAndItemContent() throws IOException, JSONException {
        ObjectMapper objectMapper = Jackson.newObjectMapper();

        Record record = new Record(new Entry("1", "ab", Instant.ofEpochMilli(1459241964336L)), new Item("ab", objectMapper.readTree("{\"a\":\"b\"}")));
        RecordView recordView = new RecordView(null, null, null, null, record);

        String result = objectMapper.writeValueAsString(recordView);

        assertThat(result, equalTo("{" +
                "\"entry-number\":\"1\"," +
                "\"item-hash\":\"sha-256:ab\"," +
                "\"entry-timestamp\":\"2016-03-29T08:59:24Z\"," +
                "\"a\":\"b\"" +
                "}"));
    }

}
