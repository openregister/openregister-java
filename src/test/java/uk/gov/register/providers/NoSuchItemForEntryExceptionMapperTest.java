package uk.gov.register.providers;

import org.junit.Test;
import uk.gov.register.core.Entry;
import uk.gov.register.exceptions.NoSuchItemForEntryException;

import javax.ws.rs.core.Response;

import java.time.Instant;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class NoSuchItemForEntryExceptionMapperTest {
    @Test
    public void toResponse_returns400Response() {
        NoSuchItemForEntryExceptionMapper noSuchItemForEntryExceptionMapper = new NoSuchItemForEntryExceptionMapper();
        Response response = noSuchItemForEntryExceptionMapper.toResponse(new NoSuchItemForEntryException(new Entry(105, "abcdeabcde", Instant.now(), "1234")));

        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.getEntity(), equalTo("No item found with item-hash: sha-256:abcdeabcde for entryNumber: 105 and key: 1234"));
    }
}
