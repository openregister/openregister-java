package uk.gov.mint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CTHandlerTest {
    @Mock
    Client client;

    @Mock
    EntryValidator entryValidator;

    @Test
    public void writesToCTServer() {
        String ctserver = "ctserver";
        Handler testObject = new CTHandler("register", ctserver, client, entryValidator);

        WebTarget mockedWebTarget = mock(WebTarget.class);
        when(client.target(ctserver)).thenReturn(mockedWebTarget);
        Invocation.Builder mockedBuilder = mock(Invocation.Builder.class);
        when(mockedWebTarget.request()).thenReturn(mockedBuilder);
        Response mockedResponse = mock(Response.class);
        when(mockedBuilder.post(any(Entity.class), eq(Response.class))).thenReturn(mockedResponse);
        when(mockedResponse.getStatusInfo()).thenReturn(Response.Status.OK);

        String payload = "{\"register\":\"value1\"}\n{\"register\":\"value2\"}";
        testObject.handle(payload);

        verify(client).target(ctserver);
        verify(mockedWebTarget, times(2)).request();
        verify(mockedBuilder, times(2)).post(any(Entity.class), any(Class.class));
        verify(mockedResponse, times(2)).getStatusInfo();
    }

    @Test
    public void doesNotWriteToCTServerIfEndpointNull() {
        String ctserver = null;
        Handler testObject = new CTHandler("register", ctserver, client, entryValidator);

        String payload = "{\"register\":\"value1\"}\n{\"register\":\"value2\"}";
        testObject.handle(payload);

        verify(client, never()).target(ctserver);
    }

    @Test
    public void doesNotWriteToCTServerIfEndpointBlank() {
        String ctserver = " ";
        Handler testObject = new CTHandler("register", ctserver, client, entryValidator);

        String payload = "{\"register\":\"value1\"}\n{\"register\":\"value2\"}";
        testObject.handle(payload);

        verify(client, never()).target(ctserver);
    }

    @Test
    public void doesNotWriteToCTServerIfClientNull() {
        String ctserver = "";
        Handler testObject = new CTHandler("register", ctserver, client, entryValidator);

        String payload = "{\"register\":\"value1\"}\n{\"register\":\"value2\"}";
        testObject.handle(payload);

        verify(client, never()).target(ctserver);
    }
}
