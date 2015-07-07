package uk.gov.mint;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.integration.DataStoreApplication;

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

    @Before
    public void setUp() throws Exception {
        messageHandler = new MessageHandler(channel, dataStore);
        when(envelope.getDeliveryTag()).thenReturn(DELIVERY_TAG);
    }

    @Test
    public void shouldAcceptJsonEntry() throws Exception {
        byte[] jsonBytes = "{\"foo\":\"bar\"}".getBytes();
        messageHandler.handleDelivery(null, envelope, null, jsonBytes);

        verify(dataStore).add(jsonBytes);
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    public void shouldNotAcceptIllFormedJsonEntry() throws Exception {
        byte[] jsonBytes = "{\"foo\":\"bar\"".getBytes();

        messageHandler.handleDelivery(null, envelope, null, jsonBytes);

        // We should ack the message as it's ill-formed
        // TODO: need some sort of dead letter queue
        verify(channel).basicAck(DELIVERY_TAG, false);

        verifyNoMoreInteractions(channel, dataStore);
    }

    @Test
    public void shouldNotAcceptJsonEntryWithUnescapedQuoteMarks() throws Exception {
        byte[] jsonBytes = "{\"foo\":\"\"\"}".getBytes();

        messageHandler.handleDelivery(null, envelope, null, jsonBytes);

        // We should ack the message as it's ill-formed
        // TODO: need some sort of dead letter queue
        verify(channel).basicAck(DELIVERY_TAG, false);

        verifyNoMoreInteractions(channel, dataStore);
    }

    @Test
    public void shouldTransformJsonToCanonicalMapFieldOrder() throws Exception {
        byte[] originalBytes = "{\"bbb\":5,\"ccc\":\"foo\",\"aaa\":\"bar\"}".getBytes();
        byte[] sortedBytes = "{\"aaa\":\"bar\",\"bbb\":5,\"ccc\":\"foo\"}".getBytes();

        messageHandler.handleDelivery(null, envelope, null, originalBytes);

        verify(dataStore).add(sortedBytes);
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    public void shouldStripWhitespaceFromJson() throws Exception {
        byte[] originalBytes = "{   \"  foo  \" \t\n : \r \"   \"}".getBytes();
        byte[] sortedBytes = "{\"  foo  \":\"   \"}".getBytes();

        messageHandler.handleDelivery(null, envelope, null, originalBytes);

        verify(dataStore).add(sortedBytes);
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    public void shouldTransformSimpleUnicodeEscapesToUnescapedValues() throws Exception {
        byte[] originalBytes = "{\"\\u0066\\u006f\\u006f\":\"bar\\n\"}".getBytes();
        byte[] canonicalBytes = "{\"foo\":\"bar\\n\"}".getBytes();

        messageHandler.handleDelivery(null, envelope, null, originalBytes);

        verify(dataStore).add(canonicalBytes);
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    public void shouldTransformComplexUnicodeEscapesToUnescapedValues() throws Exception {
        // uses MUSICAL SYMBOL G CLEF (U+1D11E) as a non-BMP character
        byte[] originalBytes = "{\"g-clef\":\"\\uD834\\uDD1E\"}".getBytes(); // note this is unicode escaped in the JSON
        byte[] canonicalBytes = String.format("{\"g-clef\":\"%s\"}", new String(Character.toChars(0x0001D11E))).getBytes();

        messageHandler.handleDelivery(null, envelope, null, originalBytes);

        verify(dataStore).add(canonicalBytes);
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    public void shouldTransformComplexUnicodeEscapesInKeysToUnescapedValues() throws Exception {
        // uses MUSICAL SYMBOL G CLEF (U+1D11E) as a non-BMP character
        byte[] originalBytes = "{\"\\uD834\\uDD1E\":\"g-clef\"}".getBytes(); // note this is unicode escaped in the JSON
        byte[] canonicalBytes = String.format("{\"%s\":\"g-clef\"}", new String(Character.toChars(0x0001D11E))).getBytes();

        messageHandler.handleDelivery(null, envelope, null, originalBytes);

        verify(dataStore).add(canonicalBytes);
        verify(channel).basicAck(DELIVERY_TAG, false);
    }
}
