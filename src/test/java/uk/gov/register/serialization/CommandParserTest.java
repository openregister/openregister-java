package uk.gov.register.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.Json;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.exceptions.SerializationFormatValidationException;
import uk.gov.register.exceptions.SerializedRegisterParseException;
import uk.gov.register.util.CanonicalJsonMapper;
import uk.gov.register.util.CanonicalJsonValidator;
import uk.gov.register.views.RegisterProof;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CommandParserTest {

    private CommandParser commandParser;

    @Before
    public void setUp() throws Exception {
        CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();
        CanonicalJsonValidator canonicalJsonValidator = new CanonicalJsonValidator();
        commandParser = new CommandParser(canonicalJsonMapper, canonicalJsonValidator);
    }

    @Test
    public void shouldParseAddItemCommand() {
        RegisterCommand registerCommand = commandParser.newCommand("add-item\t{\"address\":\"9AQZJ3M\",\"name\":\"ST LAWRENCE CHURCH REMAINS OF\"}");
        assertTrue(registerCommand instanceof AddItemCommand);
    }

    @Test
    public void shouldParseAppendEntryCommand() {
        RegisterCommand registerCommand = commandParser.newCommand("append-entry\t2016-11-02T14:45:54Z\tsha-256:22c3e102937320cb613660e88116bb937c1873ed54321966ee089e1d71fb4b7f");
        assertTrue(registerCommand instanceof AppendEntryCommand);
    }


    @Test(expected = SerializedRegisterParseException.class)
    public void shouldThrowExWhenNoHash() throws Exception {
        String line = "append-entry\t2016-10-12T17:45:19.757132";
        commandParser.newCommand(line);
    }

    @Test(expected = SerializedRegisterParseException.class)
    public void shouldThrowExWhenNoContent() throws Exception {
        String line = "add-item";
        commandParser.newCommand(line);
    }

    @Test(expected = SerializedRegisterParseException.class)
    public void shouldThrowExWhenNoHashPrefix() throws Exception {
        String line = "append-entry\t2016-10-12T17:45:19.757132\tabc123";
        commandParser.newCommand(line);
    }

    @Test(expected = SerializedRegisterParseException.class)
    public void shouldThrowExWhenUnknownCommand() throws Exception {
        String line = "unknown-command-here\tabc123";
        commandParser.newCommand(line);
    }

    @Test(expected = DateTimeParseException.class)
    public void shouldFailIfTimestampNotIso() throws Exception {
        String line = "append-entry\t20161212\tsha-256:abc123";
        commandParser.newCommand(line);
    }

    @Test(expected = SerializedRegisterParseException.class)
    public void shouldFailIfInvalidJson() throws Exception {
        String line = "add-item\t{\"address\":\"9AQZJ3K\"";
        commandParser.newCommand(line);
    }

    @Test(expected = SerializationFormatValidationException.class)
    public void shouldThrowSerializationFormatValidationExceptionWhenItemNotCanonicalized() {
        String line = "add-item\t{\"address\":\"9AQZJ3M\",\"street\":\"43070006\",\"name\":\"ST LAWRENCE CHURCH REMAINS OF\"}";

        commandParser.newCommand(line);
    }

    @Test
    public void serialise_shouldFormatEntryAsTsvLine(){
        Instant entryTimestamp = Instant.parse("2016-07-15T10:00:00Z");
        Entry entry = new Entry(1, "item-hash", entryTimestamp);

        String actualLine = commandParser.serialise(entry);

        assertThat(actualLine, equalTo("append-entry\t2016-07-15T10:00:00Z\tsha-256:item-hash\n"));
    }

    @Test
    public void serialise_shouldFormatItemAsTsvLine(){
        JsonNode itemContent = Json.read("{\"b\": \"2\",\"a\": \"1\" }", JsonNode.class);
        Item item = new Item(itemContent);

        String actualLine = commandParser.serialise(item);

        assertThat(actualLine, equalTo("add-item\t{\"a\":\"1\",\"b\":\"2\"}\n"));
    }

    @Test
    public void serialise_shouldFormatProofAsTsvLine(){
        RegisterProof registerProof = new RegisterProof("root-hash");

        String actualLine = commandParser.serialise(registerProof);

        assertThat(actualLine, equalTo("assert-root-hash\troot-hash\n"));
    }
}