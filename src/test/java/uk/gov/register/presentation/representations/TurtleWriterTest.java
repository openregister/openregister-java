package uk.gov.register.presentation.representations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.views.View;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.presentation.Record;
import uk.gov.register.presentation.config.FieldsConfiguration;
import uk.gov.register.presentation.mapper.JsonObjectMapper;
import uk.gov.register.presentation.resource.ResourceBase;

import javax.ws.rs.ext.MessageBodyWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
        turtleWriter = new TurtleWriter(requestContext, new FieldsConfiguration());
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
        String result = writeRecord(turtleWriter, record);
        assertThat(result, containsString("field:address <http://address.openregister.org/address/1111111>"));
        assertThat(result, containsString("field:name \"foo\""));
    }

    private String writeRecord(MessageBodyWriter<View> writer, Record record) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        writer.writeTo(new ResourceBase() {
        }.new SingleResultView(record), ResourceBase.SingleResultView.class, null, null, ExtraMediaType.TEXT_TTL_TYPE, null, stream);
        return stream.toString("utf-8");
    }
}
