package uk.gov.register.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;

public class ItemTest {
    @Test
    public void twoItemsAreSameIfHashIsSame() throws IOException {
        JsonNode json = new ObjectMapper().readTree("{\"key\":\"value\"}");
        Item item1 = new Item(json);
        Item item2 = new Item(json);
        Assert.assertThat(item1, equalTo(item2));
    }
}
