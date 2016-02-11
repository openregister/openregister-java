package uk.gov.register.presentation.representations;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.presentation.EntryView;
import uk.gov.register.presentation.FieldValue;
import uk.gov.register.presentation.LinkValue;
import uk.gov.register.presentation.ListValue;
import uk.gov.register.presentation.StringValue;
import uk.gov.register.presentation.config.RegistersConfiguration;
import uk.gov.register.presentation.resource.RequestContext;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class TurtleWriterTest {

    private TurtleWriter turtleWriter;

    @Before
    public void setUp() throws Exception {
        RequestContext requestContext = new RequestContext(new RegistersConfiguration(Optional.empty()), () -> "test.register.gov.uk") {
            @Override
            public String requestUrl() {
                return "http://widget.openregister.org/widget/123";
            }
        };
        turtleWriter = new TurtleWriter(requestContext, () -> "test.register.gov.uk");
    }

    @Test
    public void rendersFieldPrefixFromConfiguration() throws Exception {
        EntryView entry = new EntryView(52, "abcd", "registerName", Collections.emptyMap());

        TestOutputStream entityStream = new TestOutputStream();

        turtleWriter.writeEntriesTo(entityStream, Collections.emptySet(), Collections.singletonList(entry));

        assertThat(entityStream.contents, containsString("@prefix field: <http://field.test.register.gov.uk/field/>."));
    }

    @Test
    public void rendersEntryIdentifierFromRequestContext() throws Exception {
        EntryView entry = new EntryView(52, "abcd", "registerName", Collections.emptyMap());

        TestOutputStream entityStream = new TestOutputStream();

        turtleWriter.writeEntriesTo(entityStream, Collections.emptySet(), Collections.singletonList(entry));

        assertThat(entityStream.contents, containsString("<http://widget.openregister.org/entry/52>"));
    }

    @Test
    public void rendersLinksCorrectlyAsUrls() throws Exception {
        Map<String, FieldValue> entryMap =
                ImmutableMap.of(
                        "registered-address", new LinkValue("address", "test.register.gov.uk", "1111111"),
                        "name", new StringValue("foo")
                );

        EntryView entry = new EntryView(52, "abcd", "registerName", entryMap);

        TestOutputStream entityStream = new TestOutputStream();

        turtleWriter.writeEntriesTo(entityStream, ImmutableSet.of("company", "registered-address", "name"), Collections.singletonList(entry));


        assertThat(entityStream.contents, containsString("field:registered-address <http://address.test.register.gov.uk/address/1111111>"));
        assertThat(entityStream.contents, containsString("field:name \"foo\""));
    }

    @Test
    public void rendersLists() throws Exception {
        Map<String, FieldValue> entryMap =
                ImmutableMap.of(
                        "link-values", new ListValue(asList(new LinkValue("address", "test.register.gov.uk", "1111111"), new LinkValue("address", "test.register.gov.uk", "2222222"))),
                        "string-values", new ListValue(asList(new StringValue("value1"), new StringValue("value2"))),
                        "name", new StringValue("foo")
                );

        EntryView entry = new EntryView(52, "abcd", "registerName", entryMap);

        TestOutputStream entityStream = new TestOutputStream();

        turtleWriter.writeEntriesTo(entityStream, ImmutableSet.of("link-values", "string-values", "name"), Collections.singletonList(entry));


        assertThat(entityStream.contents, containsString("field:link-values <http://address.test.register.gov.uk/address/1111111>"));
        assertThat(entityStream.contents, containsString("field:link-values <http://address.test.register.gov.uk/address/2222222>"));
        assertThat(entityStream.contents, containsString("field:string-values \"value1\""));
        assertThat(entityStream.contents, containsString("field:string-values \"value2\""));
        assertThat(entityStream.contents, containsString("field:name \"foo\""));
    }
}
