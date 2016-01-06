package uk.gov.mint;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.FieldsConfiguration;
import uk.gov.register.RegistersConfiguration;
import uk.gov.store.EntriesUpdateDAO;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LoadHandlerTest {

    @Mock
    EntriesUpdateDAO entriesUpdateDAO;

    @Captor
    ArgumentCaptor<List<byte[]>> entriesCaptor;

    EntryValidator entryValidator = new EntryValidator(new RegistersConfiguration(Optional.empty()), new FieldsConfiguration(Optional.empty()));

    @Test
    public void handle_addsTheHashAndThenSavesInTheDatabase() throws Exception {
        LoadHandler loadHandler = new LoadHandler(entriesUpdateDAO);

        String payload1 = "{\"register\":\"value1\"}";
        String payload2 = "{\"register\":\"value2\"}";
        ObjectMapper om = new ObjectMapper();
        JsonNode entry1 = om.readTree(payload1);
        JsonNode entry2 = om.readTree(payload2);
        List<JsonNode> entries = new ArrayList<JsonNode>();
        entries.add(entry1);
        entries.add(entry2);

        String expectedHash1 = Digest.shasum(payload1);
        String expectedHash2 = Digest.shasum(payload2);

        final String expectedEntry1 = "{\"entry\":{\"register\":\"value1\"},\"hash\":\"" + expectedHash1 + "\"}";
        final byte[] expectedEntry1Bytes = canonicalise(expectedEntry1);
        final String expectedEntry2 = "{\"entry\":{\"register\":\"value2\"},\"hash\":\"" + expectedHash2 + "\"}";
        final byte[] expectedEntry2Bytes = canonicalise(expectedEntry2);

        loadHandler.load(entries);

        verify(entriesUpdateDAO, times(1)).add(entriesCaptor.capture());
        final List<byte[]> payloadArray = entriesCaptor.getValue();
        assertThat(payloadArray.size(), equalTo(2));
        assertThat(payloadArray.get(0), equalTo(expectedEntry1Bytes));
        assertThat(payloadArray.get(1), equalTo(expectedEntry2Bytes));
    }

    private byte[] canonicalise(String nonCanonical) throws IOException {
        CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();
        final JsonNode jsonNode = canonicalJsonMapper.readFromBytes(nonCanonical.getBytes(StandardCharsets.UTF_8));
        return canonicalJsonMapper.writeToBytes(jsonNode);
    }
}
