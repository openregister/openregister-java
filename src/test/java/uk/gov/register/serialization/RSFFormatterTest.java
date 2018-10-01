package uk.gov.register.serialization;

import org.junit.Before;
import org.junit.Test;
import uk.gov.register.exceptions.RSFParseException;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class RSFFormatterTest {

    private RSFFormatter rsfFormatter;

    @Before
    public void setUp() throws Exception {
        rsfFormatter = new RSFFormatter();
    }

    @Test
    public void getFileExtension_returnsRsfExtension() {
        assertThat(rsfFormatter.getFileExtension(), equalTo("rsf"));
    }

    @Test
    public void parse_parsesAddItemCommand() {
        RegisterCommand parsedCommand = rsfFormatter.parse("add-item\t{\"register\":\"ft_openregister_test\",\"text\":\"SomeText\"}");
        assertThat(parsedCommand.getCommandName(), equalTo("add-item"));
        assertThat(parsedCommand.getCommandArguments(), equalTo(Collections.singletonList("{\"register\":\"ft_openregister_test\",\"text\":\"SomeText\"}")));
    }

    @Test
    public void parse_parsesAppendEntryCommandWithSystemType() {
        RegisterCommand parsedCommand = rsfFormatter.parse("append-entry\tsystem\tft_openregister_test\t2016-11-02T14:45:54Z\tsha-256:3cee6dfc567f2157208edc4a0ef9c1b417302bad69ee06b3e96f80988b37f254");
        assertThat(parsedCommand.getCommandName(), equalTo("append-entry"));
        assertThat(parsedCommand.getCommandArguments(), equalTo(Arrays.asList("system","ft_openregister_test", "2016-11-02T14:45:54Z", "sha-256:3cee6dfc567f2157208edc4a0ef9c1b417302bad69ee06b3e96f80988b37f254")));
    }

    @Test
    public void parse_parsesAppendEntryCommand() {
        RegisterCommand parsedCommand = rsfFormatter.parse("append-entry\tuser\tft_openregister_test\t2016-11-02T14:45:54Z\tsha-256:3cee6dfc567f2157208edc4a0ef9c1b417302bad69ee06b3e96f80988b37f254");
        assertThat(parsedCommand.getCommandName(), equalTo("append-entry"));
        assertThat(parsedCommand.getCommandArguments(), equalTo(Arrays.asList("user","ft_openregister_test", "2016-11-02T14:45:54Z", "sha-256:3cee6dfc567f2157208edc4a0ef9c1b417302bad69ee06b3e96f80988b37f254")));
    }

    @Test(expected = RSFParseException.class)
    public void parse_failToParseInvalidType() {
        String line = "append-entry\tZZZZ\t2016-11-02T14:45:54Z\tsha-256:a;sha-256:b\tft_openregister_test";
        rsfFormatter.parse(line);
    }

    @Test
    public void parse_parsesAssertRootHashCommand() {
        RegisterCommand parsedCommand = rsfFormatter.parse("assert-root-hash\tsha-256:root-hash");
        assertThat(parsedCommand.getCommandName(), equalTo("assert-root-hash"));
        assertThat(parsedCommand.getCommandArguments(), equalTo(Collections.singletonList("sha-256:root-hash")));
    }

    @Test(expected = RSFParseException.class)
    public void parse_throwsExceptionWhenCommandArgumentsEmpty() throws Exception {
        rsfFormatter.parse("append-entry");
    }

    @Test(expected = RSFParseException.class)
    public void parse_throwsExceptionWhenCommandNotRecognised() throws Exception {
        rsfFormatter.parse("some-new-stuff");
    }

    @Test(expected = RSFParseException.class)
    public void parse_throwsExceptionWhenDataMissingForAppendEntry() throws Exception {
        rsfFormatter.parse("append-entry\t2016-10-12T17:45:19.757132");
    }

    @Test(expected = RSFParseException.class)
    public void parse_throwsExceptionWhenHashInWrongFormat() throws Exception {
        rsfFormatter.parse("append-entry\tuser\t2016-11-02T14:45:54Z\tsha-25:cee6dfc567f2157208edc4b417302bad69ee06b3e96f80988b37f254\tft_openregister_test");
    }


    @Test(expected = RSFParseException.class)
    public void parse_throwsExceptionWhenTimestampNotIso() throws Exception {
        rsfFormatter.parse("append-entry\tuser\t20161212\tsha-256:abc123\t123");
    }

    @Test(expected = RSFParseException.class)
    public void parse_throwsExceptionWhenInvalidJson() throws Exception {
        rsfFormatter.parse("add-item\t{\"address\":\"9AQZJ3K\"");
    }

    @Test(expected = RSFParseException.class)
    public void parse_throwsExceptionWhenItemNotCanonicalized() {
        rsfFormatter.parse("add-item\t{\"address\":\"9AQZJ3M\",\"street\":\"43070006\",\"name\":\"ST LAWRENCE CHURCH REMAINS OF\"}");
    }

    @Test
    public void format_formatAppendEntryCommandAsTsvLine() {
        RegisterCommand formattedCommand = new RegisterCommand("append-entry", Arrays.asList("user","2016-07-15T10:00:00Z", "sha-256:item-hash", "key"));
        assertThat(rsfFormatter.format(formattedCommand), equalTo("append-entry\tuser\t2016-07-15T10:00:00Z\tsha-256:item-hash\tkey\n"));
    }

    @Test
    public void format_formatsAddItemCommandAsTsvLine() {
        RegisterCommand formattedCommand = new RegisterCommand("add-item", Collections.singletonList("{\"a\":\"1\",\"b\":\"2\"}"));
        assertThat(rsfFormatter.format(formattedCommand), equalTo("add-item\t{\"a\":\"1\",\"b\":\"2\"}\n"));
    }

    @Test
    public void serialise_shouldFormatProofAsTsvLine() {
        RegisterCommand formattedCommand = new RegisterCommand("assert-root-hash", Collections.singletonList("sha-256:root-hash"));
        assertThat(rsfFormatter.format(formattedCommand), equalTo("assert-root-hash\tsha-256:root-hash\n"));
    }
}
