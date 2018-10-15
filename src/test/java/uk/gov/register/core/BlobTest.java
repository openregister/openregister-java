package uk.gov.register.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;

public class BlobTest {
    @Test
    public void twoItemsAreSameIfHashIsSame() throws IOException {
        JsonNode json = new ObjectMapper().readTree("{\"key\":\"value\"}");
        Blob blob1 = new Blob(json);
        Blob blob2 = new Blob(json);
        Assert.assertThat(blob1, equalTo(blob2));
    }
}
