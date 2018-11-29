package uk.gov.register.views.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jackson.Jackson;
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
import static org.junit.Assert.assertThat;

public class EntryListViewTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final Entry entry1 = new Entry(
            1,
            new HashValue(HashingAlgorithm.SHA256,"ab"),
            new HashValue(HashingAlgorithm.SHA256,"ab-blob-hash"),
            Instant.ofEpochSecond(1470403440),
            "b",
            EntryType.user
    );
    private final Entry entry2 = new Entry(
            2,
            new HashValue(HashingAlgorithm.SHA256,"cd"),
            new HashValue(HashingAlgorithm.SHA256,"cd-blob-hash"),
            Instant.ofEpochSecond(1470403441),
            "c",
            EntryType.user
    );
    private final EntryListView view = new EntryListView(ImmutableList.of(entry1, entry2));

    @Test
    public void jsonRepresentation() throws JsonProcessingException {
        String result = objectMapper.writeValueAsString(view);

        assertThat(result, equalTo("[" +
                "{" +
                "\"entry-number\":1," +
                "\"entry-timestamp\":\"2016-08-05T13:24:00Z\"," +
                "\"key\":\"b\"," +
                "\"blob-hash\":\"sha-256:ab-blob-hash\"" +
                "}," +
                "{" +
                "\"entry-number\":2," +
                "\"entry-timestamp\":\"2016-08-05T13:24:01Z\"," +
                "\"key\":\"c\"," +
                "\"blob-hash\":\"sha-256:cd-blob-hash\"" +
                "}" +
                "]"));
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
                        "1,2016-08-05T13:24:00Z,b,sha-256:ab-blob-hash\r\n" +
                        "2,2016-08-05T13:24:01Z,c,sha-256:cd-blob-hash\r\n"
        ));
    }
}
