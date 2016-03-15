package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.store.EntriesUpdateDAO;
import uk.gov.store.EntryStore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LoadHandlerTest {

    @Mock
    EntriesUpdateDAO entriesUpdateDAO;

    @Mock
    EntryStore entryStore;

    @Captor
    ArgumentCaptor<List<byte[]>> entriesCaptor;

    @Captor
    ArgumentCaptor<List<Item>> itemsCaptor;

    ObjectMapper om = new ObjectMapper();

    @Test
    public void handle_addsTheHashAndThenSavesOnlyInTheEntriesTable_whenMigrationIsRunning() throws Exception {
        LoadHandler loadHandler = new LoadHandler(entriesUpdateDAO, entryStore, true);

        String payload1 = "{\"register\":\"value1\"}";
        String payload2 = "{\"register\":\"value2\"}";
        List<JsonNode> entries = Arrays.asList(om.readTree(payload1), om.readTree(payload2));

        String expectedHash1 = Digest.shasum(payload1);
        String expectedHash2 = Digest.shasum(payload2);

        final String expectedEntry1 = "{\"entry\":{\"register\":\"value1\"},\"hash\":\"" + expectedHash1 + "\"}";
        final byte[] expectedEntry1Bytes = canonicalise(expectedEntry1);
        final String expectedEntry2 = "{\"entry\":{\"register\":\"value2\"},\"hash\":\"" + expectedHash2 + "\"}";
        final byte[] expectedEntry2Bytes = canonicalise(expectedEntry2);

        loadHandler.load(entries);

        verify(entriesUpdateDAO, times(1)).add(entriesCaptor.capture());
        verifyNoMoreInteractions(entryStore);
        final List<byte[]> payloadArray = entriesCaptor.getValue();
        assertThat(payloadArray.size(), equalTo(2));
        assertThat(payloadArray.get(0), equalTo(expectedEntry1Bytes));
        assertThat(payloadArray.get(1), equalTo(expectedEntry2Bytes));
    }

    @Test
    public void handle_addsTheEntriesInEntryAndItemTable_whenNoMigrationIsRunning() throws IOException {
        LoadHandler loadHandler = new LoadHandler(entriesUpdateDAO, entryStore, false);

        String payload1 = "{\"register\":\"value1\"}";
        String payload2 = "{\"register\":\"value2\"}";
        List<JsonNode> entries = Arrays.asList(om.readTree(payload1), om.readTree(payload2));

        loadHandler.load(entries);

        verify(entriesUpdateDAO, times(1)).add(entriesCaptor.capture());
        verify(entryStore).load(itemsCaptor.capture());
        List<Item> items = itemsCaptor.getValue();
        assertThat(items.size(), equalTo(2));
        assertThat(items.get(0).getCanonicalContent(), equalTo(payload1.getBytes()));
        assertThat(items.get(1).getCanonicalContent(), equalTo(payload2.getBytes()));
    }


    private byte[] canonicalise(String nonCanonical) throws IOException {
        CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();
        final JsonNode jsonNode = canonicalJsonMapper.readFromBytes(nonCanonical.getBytes(StandardCharsets.UTF_8));
        return canonicalJsonMapper.writeToBytes(jsonNode);
    }
}
