package uk.gov.register.util;

import com.fasterxml.jackson.core.JsonParseException;
import org.junit.Test;
import uk.gov.register.util.CanonicalJsonMapper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class CanonicalJsonMapperTest {
    public final CanonicalJsonMapper mapper = new CanonicalJsonMapper();

    @Test
    public void shouldAcceptJson() throws Exception {
        byte[] jsonBytes = "{\"foo\":\"bar\"}".getBytes();

        byte[] canonicalBytes = mapper.writeToBytes(mapper.readFromBytes(jsonBytes));
        
        assertThat(canonicalBytes, equalTo(jsonBytes));
    }

    @Test(expected = JsonParseException.class)
    public void shouldNotAcceptJsonWithUnbalancedBraces() throws Exception {
        byte[] jsonBytes = "{\"foo\":\"bar\"".getBytes();

        mapper.readFromBytes(jsonBytes);
    }

    @Test(expected = JsonParseException.class)
    public void shouldNotAcceptJsonWithUnescapedQuoteMarks() throws Exception {
        byte[] jsonBytes = "{\"foo\":\"\"\"}".getBytes();

        mapper.readFromBytes(jsonBytes);
    }

    @Test
    public void shouldTransformJsonToCanonicalMapFieldOrder() throws Exception {
        byte[] originalBytes = "{\"bbb\":5,\"ccc\":\"foo\",\"aaa\":\"bar\"}".getBytes();
        byte[] sortedBytes = "{\"aaa\":\"bar\",\"bbb\":5,\"ccc\":\"foo\"}".getBytes();

        byte[] canonicalBytes = mapper.writeToBytes(mapper.readFromBytes(originalBytes));

        assertThat(canonicalBytes, equalTo(sortedBytes));
    }

    @Test
    public void shouldStripWhitespaceFromJson() throws Exception {
        byte[] originalBytes = "{   \"  foo  \" \t\n : \r \"   \"}".getBytes();
        byte[] strippedBytes = "{\"  foo  \":\"   \"}".getBytes();

        byte[] canonicalBytes = mapper.writeToBytes(mapper.readFromBytes(originalBytes));

        assertThat(canonicalBytes, equalTo(strippedBytes));
    }

    @Test
    public void shouldTransformSimpleUnicodeEscapesToUnescapedValues() throws Exception {
        byte[] originalBytes = "{\"\\u0066\\u006f\\u006f\":\"bar\\n\"}".getBytes();
        byte[] unescapedBytes = "{\"foo\":\"bar\\n\"}".getBytes();

        byte[] canonicalBytes = mapper.writeToBytes(mapper.readFromBytes(originalBytes));

        assertThat(canonicalBytes, equalTo(unescapedBytes));
    }

    @Test
    public void shouldTransformComplexUnicodeEscapesToUnescapedValues() throws Exception {
        // uses MUSICAL SYMBOL G CLEF (U+1D11E) as a non-BMP character
        byte[] originalBytes = "{\"g-clef\":\"\\uD834\\uDD1E\"}".getBytes(); // note this is unicode escaped in the JSON
        byte[] unescapedBytes = String.format("{\"g-clef\":\"%s\"}", new String(Character.toChars(0x0001D11E))).getBytes();

        byte[] canonicalBytes = mapper.writeToBytes(mapper.readFromBytes(originalBytes));

        assertThat(canonicalBytes, equalTo(unescapedBytes));
    }

    @Test
    public void shouldTransformComplexUnicodeEscapesInKeysToUnescapedValues() throws Exception {
        // uses MUSICAL SYMBOL G CLEF (U+1D11E) as a non-BMP character
        byte[] originalBytes = "{\"\\uD834\\uDD1E\":\"g-clef\"}".getBytes(); // note this is unicode escaped in the JSON
        byte[] unescapedBytes = String.format("{\"%s\":\"g-clef\"}", new String(Character.toChars(0x0001D11E))).getBytes();

        byte[] canonicalBytes = mapper.writeToBytes(mapper.readFromBytes(originalBytes));

        assertThat(canonicalBytes, equalTo(unescapedBytes));
    }
}
