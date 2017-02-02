package uk.gov.register.views;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.dropwizard.jackson.Jackson;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.register.core.*;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class RecordsViewTest {
//    @Test
//    public void recordsJson_returnsTheMapOfRecords() throws IOException, JSONException {
//        Instant t1 = Instant.parse("2016-03-29T08:59:25Z");
//        Instant t2 = Instant.parse("2016-03-28T09:49:26Z");
//        ImmutableList<Field> fields = ImmutableList.of(
//                new Field("address", "datatype", new RegisterName("address"), Cardinality.ONE, "text"),
//                new Field("street", "datatype", new RegisterName("address"), Cardinality.ONE, "text"));
//
//        List<RecordView> records = Lists.newArrayList(
//                new RecordView(
//                        new Entry(1, new HashValue(HashingAlgorithm.SHA256, "ab"), t1, "123"),
//                        new ItemView(new HashValue(HashingAlgorithm.SHA256, "ab"),
//                                ImmutableMap.of("address", new StringValue("123"),
//                                        "street", new StringValue("foo")),
//                                fields),
//                        fields
//                ),
//                new RecordView(
//                        new Entry(2, new HashValue(HashingAlgorithm.SHA256, "cd"), t2, "456"),
//                        new ItemView(new HashValue(HashingAlgorithm.SHA256, "cd"),
//                                ImmutableMap.of("address", new StringValue("456"),
//                                        "street", new StringValue("bar")),
//                                fields),
//                        fields
//                )
//        );
//        RecordsView recordsView = new RecordsView(records, fields);
//
//        Map<String, RecordView> result = recordsView.recordsJson();
//        assertThat(result.size(), equalTo(2));
//
//
//        JSONAssert.assertEquals(
//                "{" +
//                        "\"entry-number\":\"1\"," +
//                        "\"item-hash\":\"sha-256:ab\"," +
//                        "\"entry-timestamp\":\"2016-03-29T08:59:25Z\"," +
//                        "\"address\":\"123\"," +
//                        "\"street\":\"foo\"" +
//                        "}",
//                Jackson.newObjectMapper().writeValueAsString(result.get("123")),
//                false
//        );
//
//        JSONAssert.assertEquals(
//                "{" +
//                        "\"entry-number\":\"2\"," +
//                        "\"item-hash\":\"sha-256:cd\"," +
//                        "\"entry-timestamp\":\"2016-03-28T09:49:26Z\"," +
//                        "\"address\":\"456\"," +
//                        "\"street\":\"bar\"" +
//                        "}",
//                Jackson.newObjectMapper().writeValueAsString(result.get("456")),
//                false
//        );
//    }


}
