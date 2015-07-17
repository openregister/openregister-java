package uk.gov.mint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.store.EntriesUpdateDAO;
import uk.gov.store.LogStream;

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

        loadHandler.handle(payload);

        verify(entriesUpdateDAO).add(("{\"entry\":{\"key1\":\"value1\"},\"hash\":\"" + expectedHash1 + "\"}").getBytes());
        verify(entriesUpdateDAO).add(("{\"entry\":{\"key2\":\"value2\"},\"hash\":\"" + expectedHash2 + "\"}").getBytes());
        verify(logStream, times(2)).notifyOfNewEntries();
    }
}