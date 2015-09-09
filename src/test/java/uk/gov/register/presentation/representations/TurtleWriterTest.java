package uk.gov.register.presentation.representations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.presentation.Record;
import uk.gov.register.presentation.config.FieldsConfiguration;
import uk.gov.register.presentation.mapper.JsonObjectMapper;
import uk.gov.register.presentation.resource.RequestContext;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class TurtleWriterTest {

    private RequestContext requestContext = new RequestContext() {
        @Override
        public String requestUrl() {
            return "http://widget.openregister.org/widget/123";
        }
    };

    private TurtleWriter turtleWriter;

    @Before
    public void setUp() throws Exception {
        turtleWriter = new TurtleWriter(requestContext);
    }

    @Test
    public void rendersLinksCorrectlyAsUrls() throws Exception {
        Map<String, String> entryMap =
                ImmutableMap.of(
                        "address", "1111111",
                        "name", "foo"
                );

        Record record = new Record("abcd", JsonObjectMapper.convert(entryMap, new TypeReference<JsonNode>() {
        }));
        record.setFieldsConfiguration(new FieldsConfiguration());

        TestOutputStream entityStream = new TestOutputStream();

        turtleWriter.writeRecordsTo(entityStream, Collections.singletonList(record));


        assertThat(entityStream.contents, containsString("field:address <http://address.openregister.org/address/1111111>"));
        assertThat(entityStream.contents, containsString("field:name \"foo\""));
    }
}
