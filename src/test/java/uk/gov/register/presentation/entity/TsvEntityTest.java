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

public class TsvEntityTest {
    @Test
    public void tsv_entriesAreEscaped() {
        TsvEntity csvEntity = new TsvEntity();


        Map jsonMap = ImmutableMap.of(
                "hash", "hash1",
                "entry", ImmutableMap.of(
                        "key1", "value1",
                        "key2", "val\tue2",
                        "key3", "val\"ue3",
                        "key4", "val\nue4"
                )
        );
        JsonNode node = JsonObjectMapper.convert(jsonMap, JsonNode.class);

        AbstractView view = new SingleResultView("someTemplate", node);
        Object result = csvEntity.convert(view);

        assertThat(result.toString(), equalTo("hash\tkey1\tkey2\tkey3\tkey4\r\nhash1\tvalue1\t\"val\tue2\"\t\"val\"\"ue3\"\t\"val\nue4\""));
    }

}
