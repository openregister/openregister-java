package uk.gov.mint;

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

    private MessageHandler messageHandler;

    @Mock
    private Channel channel;
    @Mock
    private DataStoreApplication dataStore;

    @Before
    public void setUp() throws Exception {
        messageHandler = new MessageHandler(channel, dataStore);
    }

    @Test
    public void shouldAcceptJsonEntry() throws Exception {
        byte[] jsonBytes = "{\"foo\":\"bar\"}".getBytes();
        Envelope mock = mock(Envelope.class);
        when(mock.getDeliveryTag()).thenReturn(52L);
        messageHandler.handleDelivery(null, mock, null, jsonBytes);

        verify(dataStore).add(jsonBytes);
        verify(channel).basicAck(52L, false);
    }

    @Test
    public void shouldNotAcceptIllFormedJsonEntry() throws Exception {
        byte[] jsonBytes = "{\"foo\":\"bar\"".getBytes();

        Envelope mock = mock(Envelope.class);
        when(mock.getDeliveryTag()).thenReturn(52L);

        messageHandler.handleDelivery(null, mock, null, jsonBytes);

        // We should ack the message as it's ill-formed
        // TODO: need some sort of dead letter queue
        verify(channel).basicAck(52L, false);

        verifyNoMoreInteractions(channel, dataStore);
    }

    @Test
    public void shouldNotAcceptJsonEntryWithUnescapedQuoteMarks() throws Exception {
        byte[] jsonBytes = "{\"foo\":\"\"\"}".getBytes();
        Envelope mock = mock(Envelope.class);
        when(mock.getDeliveryTag()).thenReturn(52L);
        messageHandler.handleDelivery(null, mock, null, jsonBytes);

        // We should ack the message as it's ill-formed
        // TODO: need some sort of dead letter queue
        verify(channel).basicAck(52L, false);

        verifyNoMoreInteractions(channel, dataStore);
    }
}
