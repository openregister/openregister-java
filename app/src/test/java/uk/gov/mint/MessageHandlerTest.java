package uk.gov.mint;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.integration.DataStoreApplication;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MessageHandlerTest {

    public static final long DELIVERY_TAG = 52L;
    private MessageHandler messageHandler;

    @Mock
    private Channel channel;
    @Mock
    private DataStoreApplication dataStore;
    @Mock
    private Envelope envelope;
    @Mock
    private CanonicalJsonMapper jsonMapper;

    private final byte[] incomingBytes = "incoming".getBytes();
    private final byte[] outgoingBytes = "outgoing".getBytes();
    @Mock
    private JsonNode internalJson;

    @Before
    public void setUp() throws Exception {
        messageHandler = new MessageHandler(channel, dataStore, jsonMapper);
        when(envelope.getDeliveryTag()).thenReturn(DELIVERY_TAG);

        when(jsonMapper.readFromBytes(incomingBytes)).thenReturn(internalJson);
        when(jsonMapper.writeToBytes(internalJson)).thenReturn(outgoingBytes);
    }

    @Test
    public void shouldAcknowledgeAndSaveValidMessageToDataStore() throws Exception {
        messageHandler.handleDelivery(null, envelope, null, incomingBytes);

        verify(dataStore).add(outgoingBytes);
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    public void shouldAcknowledgeAndSuppressIllFormedMessage() throws Exception {
        when(jsonMapper.readFromBytes(incomingBytes)).thenThrow(mock(JsonParseException.class));

        messageHandler.handleDelivery(null, envelope, null, incomingBytes);

        // We should ack the message because we know it can never be successfully processed
        // TODO: need some sort of dead letter queue
        verify(channel).basicAck(DELIVERY_TAG, false);

        verifyNoMoreInteractions(channel, dataStore);
    }
}
