package uk.gov.register.presentation.view;

import com.google.common.collect.Lists;
import io.dropwizard.jackson.Jackson;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.register.FieldsConfiguration;
import uk.gov.register.presentation.ItemConverter;
import uk.gov.register.presentation.dao.Entry;
import uk.gov.register.presentation.dao.Item;
import uk.gov.register.presentation.dao.Record;
import uk.gov.register.presentation.resource.RequestContext;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RecordListViewTest {
    @Mock
    RequestContext requestContext;

    @Test
    public void recordsJson_returnsTheMapOfRecords() throws IOException, JSONException {
        Instant t1 = Instant.parse("2016-03-29T08:59:25Z");
        Instant t2 = Instant.parse("2016-03-28T09:49:26Z");
        List<Record> records = Lists.newArrayList(
                new Record(
                        new Entry("1", "ab", t1),
                        new Item("ab", Jackson.newObjectMapper().readTree("{\"address\":\"123\", \"street\":\"foo\"}"))
                ),
                new Record(
                        new Entry("2", "cd", t2),
                        new Item("cd", Jackson.newObjectMapper().readTree("{\"address\":\"456\", \"street\":\"bar\"}"))
                )
        );
        when(requestContext.getRegisterPrimaryKey()).thenReturn("address");
        RecordListView recordListView = new RecordListView(requestContext, null, null, null, new ItemConverter(new FieldsConfiguration(Optional.empty()), requestContext), records);

        Map<String, RecordView> result = recordListView.recordsJson();
        assertThat(result.size(), equalTo(2));


        JSONAssert.assertEquals(
                "{" +
                        "\"entry-number\":\"1\"," +
                        "\"item-hash\":\"sha-256:ab\"," +
                        "\"entry-timestamp\":\"2016-03-29T08:59:25Z\"," +
                        "\"address\":\"123\"," +
                        "\"street\":\"foo\"" +
                        "}",
                Jackson.newObjectMapper().writeValueAsString(result.get("123")),
                false
        );

        JSONAssert.assertEquals(
                "{" +
                        "\"entry-number\":\"2\"," +
                        "\"item-hash\":\"sha-256:cd\"," +
                        "\"entry-timestamp\":\"2016-03-28T09:49:26Z\"," +
                        "\"address\":\"456\"," +
                        "\"street\":\"bar\"" +
                        "}",
                Jackson.newObjectMapper().writeValueAsString(result.get("456")),
                false
        );
    }


}
