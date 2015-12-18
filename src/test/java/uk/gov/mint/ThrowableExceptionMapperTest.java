package uk.gov.mint;

import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class ThrowableExceptionMapperTest {
    @Test
    public void toResponse_returns500Response() {
        ThrowableExceptionMapper throwableExceptionMapper = new ThrowableExceptionMapper();
        Response response = throwableExceptionMapper.toResponse(new Exception("some message"));

        assertThat(response.getStatus(), equalTo(500));
        assertThat(response.getEntity(), equalTo("some message"));
    }
}
