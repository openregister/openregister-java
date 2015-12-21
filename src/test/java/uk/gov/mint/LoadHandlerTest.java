package uk.gov.mint;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
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

    @Mock
    Client client;

    @Captor
    ArgumentCaptor<List<byte[]>> entriesCaptor;

    EntryValidator entryValidator = new EntryValidator(new RegistersConfiguration(Optional.empty()), new FieldsConfiguration(Optional.empty()));

    @Test
    public void handle_addsTheHashAndThenSavesInTheDatabase() throws Exception {
        LoadHandler loadHandler = new LoadHandler("register", "ctserver", null, entriesUpdateDAO, entryValidator);

        String payload = "{\"register\":\"value1\"}\n{\"register\":\"value2\"}";

        String expectedHash1 = Digest.shasum("{\"register\":\"value1\"}");
        String expectedHash2 = Digest.shasum("{\"register\":\"value2\"}");

        final String entry1 = "{\"entry\":{\"register\":\"value1\"},\"hash\":\"" + expectedHash1 + "\"}";
        final byte[] entry1Bytes = canonicalise(entry1);
        final String entry2 = "{\"entry\":{\"register\":\"value2\"},\"hash\":\"" + expectedHash2 + "\"}";
        final byte[] entry2Bytes = canonicalise(entry2);

        loadHandler.handle(payload);

        verify(entriesUpdateDAO, times(1)).add(entriesCaptor.capture());
        final List<byte[]> payloadArray = entriesCaptor.getValue();
        assertThat(payloadArray.size(), equalTo(2));
        assertThat(payloadArray.get(0), equalTo(entry1Bytes));
        assertThat(payloadArray.get(1), equalTo(entry2Bytes));
    }

    @Test(expected = JsonParseException.class)
    public void handle_throwsJsonParseExceptionWhenTheInputIsNotValidJsonl() {
        LoadHandler loadHandler = new LoadHandler("register", "ctserver", null, entriesUpdateDAO, entryValidator);
        String payload = "{\"register\":\n\"value1\"}";
        loadHandler.handle(payload);
    }

    @Test
    public void writesToCTServer()  {
        String ctserver = "ctserver";
        LoadHandler loadHandler = new LoadHandler("register", ctserver, client, entriesUpdateDAO, entryValidator);

        WebTarget mockedWebTarget = mock(WebTarget.class);
        when(client.target(ctserver)).thenReturn(mockedWebTarget);
        Invocation.Builder mockedBuilder = mock(Invocation.Builder.class);
        when(mockedWebTarget.request()).thenReturn(mockedBuilder);
        Response mockedResponse = mock(Response.class);
        when(mockedBuilder.post(any(Entity.class), eq(Response.class))).thenReturn(mockedResponse);
        when(mockedResponse.getStatusInfo()).thenReturn(Response.Status.OK);

        String payload = "{\"register\":\"value1\"}\n{\"register\":\"value2\"}";
        loadHandler.handle(payload);

        verify(client).target(ctserver);
        verify(mockedWebTarget, times(2)).request();
        verify(mockedBuilder, times(2)).post(any(Entity.class), any(Class.class));
        verify(mockedResponse, times(2)).getStatusInfo();
    }

    @Test
    public void doesNotWriteToCTServerIfEndpointNull()  {
        String ctserver = null;
        LoadHandler loadHandler = new LoadHandler("register", ctserver, client, entriesUpdateDAO, entryValidator);

        String payload = "{\"register\":\"value1\"}\n{\"register\":\"value2\"}";
        loadHandler.handle(payload);

        verify(client, never()).target(ctserver);
    }

    @Test
    public void doesNotWriteToCTServerIfEndpointBlank()  {
        String ctserver = " ";
        LoadHandler loadHandler = new LoadHandler("register", ctserver, client, entriesUpdateDAO, entryValidator);

        String payload = "{\"register\":\"value1\"}\n{\"register\":\"value2\"}";
        loadHandler.handle(payload);

        verify(client, never()).target(ctserver);
    }

    @Test
    public void doesNotWriteToCTServerIfClientNull()  {
        String ctserver = "";
        LoadHandler loadHandler = new LoadHandler("register", ctserver, null, entriesUpdateDAO, entryValidator);

        String payload = "{\"register\":\"value1\"}\n{\"register\":\"value2\"}";
        loadHandler.handle(payload);

        verify(client, never()).target(ctserver);
    }

    private byte[] canonicalise(String nonCanonical) throws IOException {
        CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();
        final JsonNode jsonNode = canonicalJsonMapper.readFromBytes(nonCanonical.getBytes(StandardCharsets.UTF_8));
        return canonicalJsonMapper.writeToBytes(jsonNode);
    }
}
