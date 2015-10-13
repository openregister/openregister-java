package uk.gov.register.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.presentation.config.FieldsConfiguration;
import uk.gov.register.presentation.resource.RequestContext;

import java.io.IOException;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class EntryConverterTest {
    @Mock
    private RequestContext requestContext;
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    private EntryConverter entryConverter;

    @Before
    public void setUp() throws Exception {
        entryConverter = new EntryConverter(new FieldsConfiguration(), requestContext);
    }

    @Test
    public void convert_convertsTheDbEntryToEntryView() throws IOException {
        JsonNode jsonNode = MAPPER.readValue("{\"registry\":\"somevalue\"}", JsonNode.class);

        EntryView entryView = entryConverter.convert(new DbEntry(13, new DbContent("somehash", jsonNode)));

        assertThat(((LinkValue) entryView.getField("registry").get()).link(), equalTo("http://public-body.openregister.org/public-body/somevalue"));
    }

    @Test
    public void convert_convertsListValues() throws Exception {
        JsonNode jsonNode = MAPPER.readValue("{\"fields\":[\"value1\",\"value2\"]}", JsonNode.class);

        EntryView entryView = entryConverter.convert(new DbEntry(13, new DbContent("somehash", jsonNode)));

        ListValue fields = (ListValue) entryView.getField("fields").get();

        assertThat(fields, contains(samePropertyValuesAs(new LinkValue("field", "value1")), samePropertyValuesAs(new LinkValue("field", "value2"))));
    }
}
