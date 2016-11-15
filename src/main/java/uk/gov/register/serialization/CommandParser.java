package uk.gov.register.serialization;

import com.fasterxml.jackson.core.JsonParseException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.exceptions.SerializedRegisterParseException;
import uk.gov.register.util.CanonicalJsonMapper;
import uk.gov.register.util.CanonicalJsonValidator;
import uk.gov.register.util.ObjectReconstructor;
import uk.gov.register.views.RegisterProof;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class CommandParser {
    private static final Logger LOG = LoggerFactory.getLogger(CommandParser.class);

    private final String TAB = "\t";
    private final String NEW_LINE = System.lineSeparator();
    private final ObjectReconstructor objectReconstructor;
    private final CanonicalJsonMapper canonicalJsonMapper;
    private final CanonicalJsonValidator canonicalJsonValidator;

    @Inject
    public CommandParser(ObjectReconstructor objectReconstructor, CanonicalJsonMapper canonicalJsonMapper, CanonicalJsonValidator canonicalJsonValidator) {
        this.objectReconstructor = objectReconstructor;
        this.canonicalJsonMapper = canonicalJsonMapper;
        this.canonicalJsonValidator = canonicalJsonValidator;
    }

    public RegisterCommand newCommand(String s){
        String[] parts = s.split(TAB);
        String commandName = parts[0];
        switch (commandName) {
            case "add-item":
                if (parts.length == 2) {
                    try {
                        String jsonContent = parts[1];
                        canonicalJsonValidator.validateItemStringIsCanonicalized(jsonContent);
                        String itemHash = DigestUtils.sha256Hex(jsonContent.getBytes(StandardCharsets.UTF_8));
                        Item item = new Item(itemHash, objectReconstructor.reconstruct(jsonContent));
                        return new AddItemCommand(item);
                    } catch (JsonParseException jpe){
                        LOG.error("failed to parse json: " + parts[1]);
                        throw new SerializedRegisterParseException("failed to parse json: " + parts[1], jpe);
                    } catch (IOException e) {
                        LOG.error("",e);
                        throw new UncheckedIOException(e);
                    }
                } else {
                    LOG.error("add item line must have 2 elements, was: " + s);
                    throw new SerializedRegisterParseException("add item line must have 2 elements, was: " + s);
                }
            case "append-entry":
                if (parts.length == 3) {
                    Entry entry = new Entry(0, stripPrefix(parts[2]), Instant.parse(parts[1]));
                    return new AppendEntryCommand(entry);
                } else {
                    LOG.error("append entry line must have 3 elements, was : " + s);
                    throw new SerializedRegisterParseException("append entry line must have 3 elements, was : " + s);
                }
            case "assert-root-hash":
                LOG.error("assert-root-hash not yet supported");
                throw new NotImplementedException("assert-root-hash not yet supported");
            default:
                LOG.error("line must begin with legal command not:" + commandName);
                throw new SerializedRegisterParseException("line must begin with legal command not: " + commandName);
        }
    }

    private  String stripPrefix(String hashField) {
        if (!hashField.startsWith("sha-256:")) {
            LOG.error("hash field must start with sha-256: not:" + hashField);
            throw new SerializedRegisterParseException("hash field must start with sha-256: not: " + hashField);
        } else {
            return hashField.substring(8);
        }
    }

    public String serialise(Entry entry){
        return "append-entry" + TAB + entry.getTimestampAsISOFormat() + TAB + entry.getItemHash() + NEW_LINE;
    }
    public String serialise(Item item){
        return "add-item" + TAB + canonicalJsonMapper.writeToString(item.getContent()) + NEW_LINE;
    }
    public String serialise(RegisterProof registerProof){
        return "assert-root-hash" + TAB + registerProof.getRootHash() + NEW_LINE;
    }
    public String getFileExtension(){
        return "tsv";
    }
}
