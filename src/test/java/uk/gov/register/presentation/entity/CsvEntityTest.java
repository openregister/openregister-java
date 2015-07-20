package uk.gov.register.presentation.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.register.presentation.mapper.JsonObjectMapper;
import uk.gov.register.presentation.view.AbstractView;
import uk.gov.register.presentation.view.SingleResultView;

import java.util.Map;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class CsvEntityTest {
    @Test
    public void csv_entriesAreEscaped() {
        CsvEntity csvEntity = new CsvEntity();

        Map jsonMap = ImmutableMap.of(
                "hash", "hash1",
                "entry", ImmutableMap.of(
                        "key1", "valu\te1",
                        "key2", "val,ue2",
                        "key3", "val\"ue3",
                        "key4", "val\nue4"
                )
        );
        JsonNode node = JsonObjectMapper.convert(jsonMap, JsonNode.class);

        AbstractView view = new SingleResultView("foo", node);
        Object result = csvEntity.convert(view);

        assertThat(result.toString(), equalTo("hash,key1,key2,key3,key4\nhash1,valu\te1,\"val,ue2\",\"val\"\"ue3\",\"val\nue4\""));
    }

}
