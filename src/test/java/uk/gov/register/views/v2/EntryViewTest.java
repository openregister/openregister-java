package uk.gov.register.views.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.representations.CsvWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class EntryViewTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final Entry entry = new Entry(
            1,
            new HashValue(HashingAlgorithm.SHA256,"ab"),
            Instant.ofEpochSecond(1470403440),
            "b",
            EntryType.user
    );
    private final EntryView view = new EntryView(entry);

    @Test
    public void jsonRepresentation() throws JsonProcessingException {
        String result = objectMapper.writeValueAsString(view);

        assertThat(result, equalTo("{" +
                "\"entry-number\":1," +
                "\"entry-timestamp\":\"2016-08-05T13:24:00Z\"," +
                "\"key\":\"b\"," +
                "\"blob-hash\":\"sha-256:ab\"" +
                "}"));
    }

    @Test
    public void csvRepresentation() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CsvWriter csvWriter = new CsvWriter();
        csvWriter.writeTo(view,
                EntryView.class,
                null,
                null,
                null,
                null,
                outputStream);
        byte[] bytes = outputStream.toByteArray();
        String result = new String(bytes, StandardCharsets.UTF_8);

        assertThat(result, equalTo(
                "entry-number,entry-timestamp,key,blob-hash\r\n" +
                "1,2016-08-05T13:24:00Z,b,sha-256:ab\r\n"
        ));
    }
}
