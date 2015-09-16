package uk.gov.register.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.presentation.config.FieldsConfiguration;
import uk.gov.register.presentation.resource.RequestContext;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class EntryConverterTest {
    @Mock
    private RequestContext requestContext;

    @Test
    public void convert_convertsTheDbEntryToEntryView() throws IOException {
        EntryConverter entryConverter = new EntryConverter(new FieldsConfiguration(), requestContext);
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        JsonNode jsonNode = objectMapper.readValue("{\"registry\":\"somevalue\"}", JsonNode.class);

        EntryView entryView = entryConverter.convert(new DbEntry("somehash", jsonNode));

        assertThat(((LinkValue) entryView.getField("registry")).link(), equalTo("http://public-body.openregister.org/public-body/somevalue"));
    }

}
