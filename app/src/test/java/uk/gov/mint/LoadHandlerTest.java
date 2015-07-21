package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.store.EntriesUpdateDAO;
import uk.gov.store.LogStream;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LoadHandlerTest {

    @Mock
    EntriesUpdateDAO entriesUpdateDAO;

    @Mock
    LogStream logStream;

    @Test
    public void handle_addsTheHashAndThenSavesInTheDatabase() throws Exception {
        LoadHandler loadHandler = new LoadHandler(entriesUpdateDAO, logStream);

        String payload = "{\"key1\":\"value1\"}\n{\"key2\":\"value2\"}";

        String expectedHash1 = Digest.shasum("{\"key1\":\"value1\"}");
        String expectedHash2 = Digest.shasum("{\"key2\":\"value2\"}");

        final String entry1 = "{\"entry\":{\"key1\":\"value1\"},\"hash\":\"" + expectedHash1 + "\"}";
        final byte[] entry1Bytes = canonicalise(entry1);
        final String entry2 = "{\"entry\":{\"key2\":\"value2\"},\"hash\":\"" + expectedHash2 + "\"}";
        final byte[] entry2Bytes = canonicalise(entry2);

        loadHandler.handle(payload);

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(entriesUpdateDAO, times(1)).add(captor.capture());
        final List<byte[]> payloadArray = captor.getValue();
        assertThat(payloadArray.size(), equalTo(2));
        assertThat(payloadArray.get(0), equalTo(entry1Bytes));
        assertThat(payloadArray.get(1), equalTo(entry2Bytes));
        verify(logStream, times(1)).notifyOfNewEntries();
    }

    private byte[] canonicalise(String nonCanonical) throws IOException {
        CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();
        final JsonNode jsonNode = canonicalJsonMapper.readFromBytes(nonCanonical.getBytes("UTF-8"));
        return canonicalJsonMapper.writeToBytes(jsonNode);
    }
}
