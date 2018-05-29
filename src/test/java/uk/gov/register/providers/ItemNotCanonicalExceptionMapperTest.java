package uk.gov.register.providers;

import org.junit.Test;
import uk.gov.register.exceptions.ItemNotCanonicalException;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ItemNotCanonicalExceptionMapperTest {
    @Test
    public void toResponse_returns400Response() {
        String jsonString = "{\"text\":\"some text\",\"register\":\"aregister\"}";
        SerializationFormatValidationExceptionMapper serializationFormatValidationExceptionMapper = new SerializationFormatValidationExceptionMapper();
        Response response = serializationFormatValidationExceptionMapper.toResponse(new ItemNotCanonicalException(jsonString));

        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.getEntity(), equalTo("Item in serialization format is not canonicalized: '"+ jsonString + "'"));
    }
}
