package uk.gov.register.serialization;

import com.fasterxml.jackson.core.JsonParseException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.core.Entry;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.exceptions.OrphanItemException;
import uk.gov.register.exceptions.SerializedRegisterParseException;
import uk.gov.register.util.CanonicalJsonMapper;
import uk.gov.register.util.CanonicalJsonValidator;
import uk.gov.register.util.HashValue;
import uk.gov.register.util.ObjectReconstructor;
import uk.gov.register.views.RegisterProof;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;

public class RSFFormat {
    private static final Logger LOG = LoggerFactory.getLogger(RSFFormat.class);

    private final String TAB = "\t";
    private final String NEW_LINE = System.lineSeparator();
    private final ObjectReconstructor objectReconstructor;
    private final CanonicalJsonMapper canonicalJsonMapper;
    private final CanonicalJsonValidator canonicalJsonValidator;

    private Integer position;
    private final HashMap<Integer, Item> items;
    private final HashMap<Integer, Entry> entries;
    private final HashMap<HashValue, Integer> itemHashToEntryCount;
    private final HashMap<Integer, RegisterProof> proofs;

    public RSFFormat() {
        position = 0;
        objectReconstructor = new ObjectReconstructor();
        canonicalJsonMapper = new CanonicalJsonMapper();
        canonicalJsonValidator = new CanonicalJsonValidator();
        items = new HashMap<>();
        entries = new HashMap<>();
        itemHashToEntryCount = new HashMap<>();
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
                        HashValue hash = new HashValue(HashingAlgorithm.SHA256, itemHash);
                        Item item = new Item(hash, objectReconstructor.reconstruct(jsonContent));
                        items.put(position++, item);
                        itemHashToEntryCount.put(hash, 0);
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
                if (parts.length == 4) {
                    Entry entry = new Entry(0, HashValue.decode(HashingAlgorithm.SHA256, parts[2]), Instant.parse(parts[1]), parts[3]);
                    entries.put(position++, entry);
                    updateItemHashCount(entry.getSha256hex());
                } else {
                    LOG.error("append entry line must have 4 elements, was : " + s);
                    throw new SerializedRegisterParseException("append entry line must have 4 elements, was : " + s);
                }
                break;
            case "assert-root-hash":
                if (parts.length == 2) {
                    HashValue hash = HashValue.decode(HashingAlgorithm.SHA256, parts[1]);
                    RegisterProof registerProof = new RegisterProof(hash);
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

    public String format(RegisterCommand2 command) {
        throw new NotImplementedException("here be not implemented dragons");
    }

    public RegisterCommand2 parse(String str) throws IllegalArgumentException {
        List<String> parts = Arrays.asList(str.split(TAB));

        String commandName = parts.get(0);
//        switch (commandName) {
//            case "add-item":
//                if (parts.size() == 2) {
////                    try {
//                        String jsonContent = parts.get(1);
//                        canonicalJsonValidator.validateItemStringIsCanonicalized(jsonContent);
//                        String itemHash = DigestUtils.sha256Hex(jsonContent.getBytes(StandardCharsets.UTF_8));
//                        HashValue hash = new HashValue(HashingAlgorithm.SHA256, itemHash);
//                        itemHashToEntryCount.put(hash, 0);
//
////                    } catch (JsonParseException jpe) {
////                        LOG.error("failed to parse json: " + parts.get(1));
////                        throw new SerializedRegisterParseException("failed to parse json: " + parts.get(1), jpe);
////                    } catch (IOException e) {
////                        LOG.error("", e);
////                        throw new UncheckedIOException(e);
////                    }
//                } else {
//                    LOG.error("add item line must have 2 elements, was: " + str);
//                    throw new SerializedRegisterParseException("add item line must have 2 elements, was: " + str);
//                }
//                break;
//            case "append-entry":
//                if (parts.size() == 4) {
//                    HashValue hashValue = HashValue.decode(HashingAlgorithm.SHA256, parts.get(2));
//                } else {
//                    LOG.error("append entry line must have 4 elements, was : " + str);
//                    throw new SerializedRegisterParseException("append entry line must have 4 elements, was : " + str);
//                }
//            default:
//                LOG.error("line must begin with legal command not:" + commandName);
////                throw new SerializedRegisterParseException("line must begin with legal command not: " + commandName);
//        }
//



        RegisterCommand2 parsedCommand = new RegisterCommand2(commandName, parts.subList(1, parts.size()));

        validate(parsedCommand);


        return parsedCommand;
    }

    private void validate(RegisterCommand2 command) throws IllegalArgumentException {

    }


    private void updateItemHashCount(HashValue hash) {
        if (itemHashToEntryCount.containsKey(hash)) {
            Integer count = itemHashToEntryCount.get(hash);
            itemHashToEntryCount.replace(hash, count + 1);
        }
    }

    public Iterator<RegisterCommand> getCommands() {
        validateOrphanItems();

        return IntStream.range(0, position).mapToObj(i -> {
            if (entries.containsKey(i)) {
                return new AppendEntryCommand(entries.get(i));
            }
            if (items.containsKey(i)) {
                return new AddItemCommand(items.get(i));
            }
            if (proofs.containsKey(i)) {
                return new AssertRootHashCommand(proofs.get(i));
            }
            throw new RuntimeException("No command found for position " + String.valueOf(i));
        }).iterator();
    }

    private void validateOrphanItems() {
        final Set<HashValue> orphanItemHashes = itemHashToEntryCount.entrySet().stream().filter(kv -> kv.getValue() == 0)
                .map(Map.Entry::getKey).collect(toSet());

        if (!orphanItemHashes.isEmpty()) {

            Set<Item> orphanItems = items.values().stream().filter(i -> orphanItemHashes.contains(i.getSha256hex()))
                    .collect(toSet());

            throw new OrphanItemException("no corresponding entry for item(s): ", orphanItems);
        }
    }

    public String serialise(Entry entry) {
        return "append-entry" + TAB + entry.getTimestampAsISOFormat() + TAB + entry.getSha256hex() + TAB + entry.getKey() + NEW_LINE;
    }

    public String serialise(Item item) {
        return "add-item" + TAB + canonicalJsonMapper.writeToString(item.getContent()) + NEW_LINE;
    }

    public String serialise(RegisterProof registerProof) {
        return "assert-root-hash" + TAB + registerProof.getRootHash() + NEW_LINE;
    }

    public String getFileExtension() {
        return "rsf";
    }
}
