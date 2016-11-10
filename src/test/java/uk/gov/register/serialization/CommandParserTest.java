package uk.gov.register.serialization;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.exceptions.SerializedRegisterParseException;
import uk.gov.register.resources.RegisterCommandReader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeParseException;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class CommandParserTest {

    private CommandParser commandParser;

    @Before
    public void setUp() throws Exception {
        commandParser = new CommandParser();
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

    @Test(expected = NotImplementedException.class)
    public void shouldThrowExWhenInvalidCommand() throws Exception {
        String line = "assert-root-hash\tabc123";
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


}