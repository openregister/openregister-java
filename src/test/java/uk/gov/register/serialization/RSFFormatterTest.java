//package uk.gov.register.serialization;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.github.tomakehurst.wiremock.common.Json;
//import org.junit.Before;
//import org.junit.Test;
//import uk.gov.register.core.Entry;
//import uk.gov.register.core.HashingAlgorithm;
//import uk.gov.register.core.Item;
//import uk.gov.register.exceptions.OrphanItemException;
//import uk.gov.register.exceptions.SerializationFormatValidationException;
//import uk.gov.register.exceptions.SerializedRegisterParseException;
//import uk.gov.register.util.HashValue;
//import uk.gov.register.views.RegisterProof;
//
//import java.time.Instant;
//import java.time.format.DateTimeParseException;
//import java.util.Iterator;
//
//import static org.hamcrest.Matchers.equalTo;
//import static org.hamcrest.core.Is.is;
//import static org.junit.Assert.assertThat;
//import static org.junit.Assert.assertTrue;
//
//public class RSFFormatterTest {
//
//    private RSFFormatter rsfFormatter;
//
//    @Before
//    public void setUp() throws Exception {
//        rsfFormatter = new RSFFormatter();
//    }
//
//    @Test
//    public void shouldParseAddItemCommand() {
//        rsfFormatter.addCommand("add-item\t{\"register\":\"ft_openregister_test\",\"text\":\"SomeText\"}");
//        rsfFormatter.addCommand("append-entry\t2016-11-02T14:45:54Z\tsha-256:3cee6dfc567f2157208edc4a0ef9c1b417302bad69ee06b3e96f80988b37f254\tft_openregister_test");
//        Iterator<RegisterCommand> registerCommands = rsfFormatter.getCommands();
//        assertTrue(registerCommands.hasNext());
//        assertTrue(registerCommands.next() instanceof AddItemCommand);
//        assertTrue(registerCommands.hasNext());
//        assertTrue(registerCommands.next() instanceof AppendEntryCommand);
//    }
//
//    @Test(expected = SerializedRegisterParseException.class)
//    public void shouldThrowExWhenNoHash() throws Exception {
//        String line = "append-entry\t2016-10-12T17:45:19.757132";
//        rsfFormatter.addCommand(line);
//    }
//
//    @Test(expected = SerializedRegisterParseException.class)
//    public void shouldThrowExWhenNoContent() throws Exception {
//        String line = "add-item";
//        rsfFormatter.addCommand(line);
//    }
//
//    @Test(expected = SerializedRegisterParseException.class)
//    public void shouldThrowExWhenNoHashPrefix() throws Exception {
//        String line = "append-entry\t2016-10-12T17:45:19.757132\tabc123";
//        rsfFormatter.addCommand(line);
//    }
//
//    @Test(expected = SerializedRegisterParseException.class)
//    public void shouldThrowExWhenUnknownCommand() throws Exception {
//        String line = "unknown-command-here\tabc123";
//        rsfFormatter.addCommand(line);
//    }
//
//    @Test(expected = DateTimeParseException.class)
//    public void shouldFailIfTimestampNotIso() throws Exception {
//        String line = "append-entry\t20161212\tsha-256:abc123\t123";
//        rsfFormatter.addCommand(line);
//    }
//
//    @Test(expected = SerializedRegisterParseException.class)
//    public void shouldFailIfInvalidJson() throws Exception {
//        String line = "add-item\t{\"address\":\"9AQZJ3K\"";
//        rsfFormatter.addCommand(line);
//    }
//
//    @Test
//    public void shouldFailForOrphanItem() throws Exception {
//        try {
//            rsfFormatter.addCommand("add-item\t{\"register\":\"ft_openregister_test\",\"text\":\"SomeText\"}");
//            rsfFormatter.addCommand("add-item\t{\"register\":\"ft_openregister_test\",\"text\":\"orphan\"}");
//            rsfFormatter.addCommand("append-entry\t2016-11-02T14:45:54Z\tsha-256:3cee6dfc567f2157208edc4a0ef9c1b417302bad69ee06b3e96f80988b37f254\tft_openregister_test");
//            rsfFormatter.getCommands();
//        } catch (OrphanItemException e) {
//            assertThat(e.getErrorJson().toString(), is("{\"message\":\"no corresponding entry for item(s): \",\"orphanItems\":[{\"register\":\"ft_openregister_test\",\"text\":\"orphan\"}]}"));
//        }
//    }
//
//    @Test(expected = OrphanItemException.class)
//    public void shouldFailForItemAfterEntry() throws Exception {
//        rsfFormatter.addCommand("append-entry\t2016-11-02T14:45:54Z\tsha-256:3cee6dfc567f2157208edc4a0ef9c1b417302bad69ee06b3e96f80988b37f254\tft_openregister_test");
//        rsfFormatter.addCommand("add-item\t{\"register\":\"ft_openregister_test\",\"text\":\"SomeText\"}");
//        rsfFormatter.getCommands();
//    }
//
//    @Test(expected = SerializationFormatValidationException.class)
//    public void shouldThrowSerializationFormatValidationExceptionWhenItemNotCanonicalized() {
//        String line = "add-item\t{\"address\":\"9AQZJ3M\",\"street\":\"43070006\",\"name\":\"ST LAWRENCE CHURCH REMAINS OF\"}";
//        rsfFormatter.addCommand(line);
//    }
//
//    @Test
//    public void serialise_shouldFormatEntryAsTsvLine() {
//        Instant entryTimestamp = Instant.parse("2016-07-15T10:00:00Z");
//        Entry entry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "item-hash"), entryTimestamp, "key");
//
//        String actualLine = rsfFormatter.serialise(entry);
//
//        assertThat(actualLine, equalTo("append-entry\t2016-07-15T10:00:00Z\tsha-256:item-hash\tkey\n"));
//    }
//
//    @Test
//    public void serialise_shouldFormatItemAsTsvLine() {
//        JsonNode itemContent = Json.read("{\"b\": \"2\",\"a\": \"1\" }", JsonNode.class);
//        Item item = new Item(itemContent);
//
//        String actualLine = rsfFormatter.serialise(item);
//
//        assertThat(actualLine, equalTo("add-item\t{\"a\":\"1\",\"b\":\"2\"}\n"));
//    }
//
//    @Test
//    public void serialise_shouldFormatProofAsTsvLine() {
//        RegisterProof registerProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, "root-hash"));
//
//        String actualLine = rsfFormatter.serialise(registerProof);
//
//        assertThat(actualLine, equalTo("assert-root-hash\tsha-256:root-hash\n"));
//    }
//}