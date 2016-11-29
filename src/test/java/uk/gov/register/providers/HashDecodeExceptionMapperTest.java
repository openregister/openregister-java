package uk.gov.register.providers;

import org.junit.Test;
import uk.gov.register.exceptions.HashDecodeException;
import javax.ws.rs.core.Response;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class HashDecodeExceptionMapperTest {
    @Test
    public void toResponse_returnsHashDecodeException() {
        HashDecodeExceptionMapper exceptionMapper = new HashDecodeExceptionMapper();
        HashDecodeException exception = new HashDecodeException("Wrong hashing algorithm specified");

        Response response = exceptionMapper.toResponse(exception);

        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.getEntity(), equalTo("Hash decode exception: Wrong hashing algorithm specified"));
    }
}