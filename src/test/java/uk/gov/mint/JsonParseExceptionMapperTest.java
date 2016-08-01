package uk.gov.mint;

import com.fasterxml.jackson.core.JsonParseException;
import org.junit.Test;
import uk.gov.register.providers.JsonParseExceptionMapper;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class JsonParseExceptionMapperTest {
    @Test
    public void toResponse_returns400Response() {
        JsonParseExceptionMapper jsonParseExceptionMapper = new JsonParseExceptionMapper();
        Response response = jsonParseExceptionMapper.toResponse(new JsonParseException("some message", null));

        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.getEntity(), equalTo("Error parsing json input: some message"));
    }
}
