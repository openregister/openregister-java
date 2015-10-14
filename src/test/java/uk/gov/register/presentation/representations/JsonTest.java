package uk.gov.register.presentation.representations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jackson.Jackson;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.register.presentation.EntryView;
import uk.gov.register.presentation.ListValue;
import uk.gov.register.presentation.StringValue;

import java.io.IOException;

public class JsonTest {
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void can_write_entries_with_lists() throws IOException, JSONException {
        EntryView entry = new EntryView(52, "hash1", "registerName", ImmutableMap.of(
                "key1", new StringValue("value1"),
                "key2", new StringValue("value2"),
                "key3", new ListValue(ImmutableList.of(new StringValue("value3"), new StringValue("value4")))
                ));

        String valueAsString = MAPPER.writeValueAsString(entry);

        JSONAssert.assertEquals("{" +
                "\"hash\":\"hash1\"," +
                "\"entry\":{" +
                "\"key1\":\"value1\"," +
                "\"key2\":\"value2\"," +
                "\"key3\":[\"value3\",\"value4\"]," +
                "}" +
                "}",valueAsString, false);
    }

}
