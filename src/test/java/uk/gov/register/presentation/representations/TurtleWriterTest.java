package uk.gov.register.presentation.representations;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.presentation.EntryView;
import uk.gov.register.presentation.FieldValue;
import uk.gov.register.presentation.LinkValue;
import uk.gov.register.presentation.StringValue;
import uk.gov.register.presentation.config.PublicBodiesConfiguration;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.config.Register;
import uk.gov.register.presentation.config.RegistersConfiguration;
import uk.gov.register.presentation.resource.RequestContext;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class TurtleWriterTest {

    private TurtleWriter turtleWriter;

    @Before
    public void setUp() throws Exception {
        RequestContext requestContext = new RequestContext(new RegistersConfiguration(new PublicBodiesConfiguration())) {
            @Override
            public String requestUrl() {
                return "http://widget.openregister.org/widget/123";
            }
        };
        turtleWriter = new TurtleWriter(requestContext);
    }

    @Test
    public void rendersLinksCorrectlyAsUrls() throws Exception {
        Map<String, FieldValue> entryMap =
                ImmutableMap.of(
                        "registered-address", new LinkValue("address", "1111111"),
                        "name", new StringValue("foo")
                );

        EntryView entry = new EntryView(52, "abcd", "registerName", entryMap);

        TestOutputStream entityStream = new TestOutputStream();

        turtleWriter.writeEntriesTo(entityStream, new Register("company", ImmutableSet.of("company", "registered-address", "name"), "", new PublicBody("Companies House", "companies-house"), ""), Collections.singletonList(entry));


        assertThat(entityStream.contents, containsString("field:registered-address <http://address.openregister.org/address/1111111>"));
        assertThat(entityStream.contents, containsString("field:name \"foo\""));
    }
}
