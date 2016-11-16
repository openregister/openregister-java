package uk.gov.register.serialization;

import com.fasterxml.jackson.core.JsonParseException;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.exceptions.OrphanItemException;
import uk.gov.register.exceptions.SerializedRegisterParseException;
import uk.gov.register.util.CanonicalJsonMapper;
import uk.gov.register.util.CanonicalJsonValidator;
import uk.gov.register.util.ObjectReconstructor;
import uk.gov.register.views.RegisterProof;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;

public class CommandParser {
    private static final Logger LOG = LoggerFactory.getLogger(CommandParser.class);

    private final String TAB = "\t";
    private final String NEW_LINE = System.lineSeparator();
    private final ObjectReconstructor objectReconstructor;
    private final CanonicalJsonMapper canonicalJsonMapper;
    private final CanonicalJsonValidator canonicalJsonValidator;

    private Integer position;
    private final HashMap<Integer, Item> items;
    private final HashMap<Integer, Entry> entries;
    private final HashMap<String, List<Integer>> entryHashes;
    private final HashMap<Integer, RegisterProof> proofs;

    public CommandParser() {
        objectReconstructor = new ObjectReconstructor();
        canonicalJsonMapper = new CanonicalJsonMapper();
        canonicalJsonValidator = new CanonicalJsonValidator();
        position = 0;
        items = new HashMap<>();
        entries = new HashMap<>();
        entryHashes = new HashMap<>();
        proofs = new HashMap<>();
    }


    public void addCommand(String s) {
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
                        items.put(position++, item);
                    } catch (JsonParseException jpe) {
                        LOG.error("failed to parse json: " + parts[1]);
                        throw new SerializedRegisterParseException("failed to parse json: " + parts[1], jpe);
                    } catch (IOException e) {
                        LOG.error("", e);
                        throw new UncheckedIOException(e);
                    }
                } else {
                    LOG.error("add item line must have 2 elements, was: " + s);
                    throw new SerializedRegisterParseException("add item line must have 2 elements, was: " + s);
                }
                break;
            case "append-entry":
                if (parts.length == 3) {
                    Entry entry = new Entry(0, stripPrefix(parts[2]), Instant.parse(parts[1]));
                    entries.put(position, entry);
                    updateEntryHashes(entry.getSha256hex(), position++);
                } else {
                    LOG.error("append entry line must have 3 elements, was : " + s);
                    throw new SerializedRegisterParseException("append entry line must have 3 elements, was : " + s);
                }
                break;
            case "assert-root-hash":
                if (parts.length == 2) {
                    RegisterProof registerProof = new RegisterProof(parts[1]);
                    proofs.put(position++, registerProof);
                } else {
                    LOG.error("assert root hash line must have 1 elements, was : " + s);
                    throw new SerializedRegisterParseException("assert root hash line must have 1 elements, was : " + s);
                }
                break;
            default:
                LOG.error("line must begin with legal command not:" + commandName);
                throw new SerializedRegisterParseException("line must begin with legal command not: " + commandName);
        }
    }

    public Iterator<RegisterCommand> getCommands() {

        validateOrphanItems();

        return IntStream.range(0, position).mapToObj(i -> {
            if (entries.containsKey(i)) {
                return new AppendEntryCommand(entries.get(i));
            } else if (items.containsKey(i)) {
                return new AddItemCommand(items.get(i));
            } else if (proofs.containsKey(i)) {
                return new AssertRootHashCommand(proofs.get(i));
            } else {
                throw new RuntimeException("No command found for position " + String.valueOf(i));
            }
        }).iterator();
    }

    private void validateOrphanItems() {
        Set<Item> orphanItems = items.entrySet().stream().filter(kv -> {
            // item must be referenced by entry below it
            Integer itemPosition = kv.getKey();
            return !entryHashes.containsKey(kv.getValue().getSha256hex()) ||
                    entryHashes.get(kv.getValue().getSha256hex()).stream().allMatch(p -> p < itemPosition);
        }).map(kv -> kv.getValue()).collect(toSet());

        if (!orphanItems.isEmpty()) {
            throw new OrphanItemException("no corresponding entry for item(s): ", orphanItems);
        }
    }

    private String stripPrefix(String hashField) {
        if (!hashField.startsWith("sha-256:")) {
            LOG.error("hash field must start with sha-256: not:" + hashField);
            throw new SerializedRegisterParseException("hash field must start with sha-256: not: " + hashField);
        } else {
            return hashField.substring(8);
        }
    }

    private void updateEntryHashes(String hash, Integer pos) {
        if (entryHashes.containsKey(hash)) {
            entryHashes.get(hash).add(pos);
        } else {
            ArrayList<Integer> list = new ArrayList<>();
            list.add(pos);
            entryHashes.put(hash, list);
        }
    }

    public String serialise(Entry entry) {
        return "append-entry" + TAB + entry.getTimestampAsISOFormat() + TAB + entry.getItemHash() + NEW_LINE;
    }

    public String serialise(Item item) {
        return "add-item" + TAB + canonicalJsonMapper.writeToString(item.getContent()) + NEW_LINE;
    }

    public String serialise(RegisterProof registerProof) {
        return "assert-root-hash" + TAB + registerProof.getRootHash() + NEW_LINE;
    }

    public String getFileExtension() {
        return "tsv";
    }
}
