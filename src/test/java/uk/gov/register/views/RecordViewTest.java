package uk.gov.register.views;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jackson.Jackson;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.core.*;
import uk.gov.register.service.BlobConverter;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RecordViewTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private RecordView recordView;

    @Before
    public void setup() throws IOException {
        BaseEntry entry = new BaseEntry(1, new HashValue(HashingAlgorithm.SHA256, "ab"), Instant.ofEpochSecond(1470403440), "b", EntryType.user);
        Blob blob = new Blob(new HashValue(HashingAlgorithm.SHA256, "ab"), objectMapper.readTree("{\"a\":\"b\"}"));
        Blob blob2 = new Blob(new HashValue(HashingAlgorithm.SHA256, "cd"), objectMapper.readTree("{\"a\":\"d\"}"));
        Record record = new Record(entry, Arrays.asList(blob, blob2));

        BlobConverter blobConverter = mock(BlobConverter.class);
        Map<String, Field> fieldsByName = mock(Map.class);
        when(blobConverter.convertBlob(blob, fieldsByName)).thenReturn(ImmutableMap.of("a", new StringValue("b")));
        when(blobConverter.convertBlob(blob2, fieldsByName)).thenReturn(ImmutableMap.of("a", new StringValue("d")));

        recordView = new RecordView(record, fieldsByName, blobConverter);
    }

    @Test
    public void recordJsonRepresentation_isJsonOfEntryAndItemContent() throws JsonProcessingException {
        String result = objectMapper.writeValueAsString(recordView);

        assertThat(result, equalTo("{" +
                "\"b\":{" +
                "\"index-entry-number\":\"1\"," +
                "\"entry-number\":\"1\"," +
                "\"entry-timestamp\":\"2016-08-05T13:24:00Z\"," +
                "\"key\":\"b\"," +
                "\"item\":[{\"a\":\"b\"},{\"a\":\"d\"}]" +
                "}}"));
    }

    @Test
    public void recordJsonRepresentation_isFlatJsonOfEntryAndItemContent() {
        String result = recordView.getFlatRecordsJson().toString();

        assertThat(result, equalTo("[{" +
                "\"index-entry-number\":\"1\"," +
                "\"entry-number\":\"1\"," +
                "\"entry-timestamp\":\"2016-08-05T13:24:00Z\"," +
                "\"key\":\"b\"," +
                "\"a\":\"b\"}," +
                "{\"index-entry-number\":\"1\"," +
                "\"entry-number\":\"1\"," +
                "\"entry-timestamp\":\"2016-08-05T13:24:00Z\"," +
                "\"key\":\"b\"," +
                "\"a\":\"d\"" +
                "}]"));
    }
}
