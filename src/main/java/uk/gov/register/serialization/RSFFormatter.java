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
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;

public class RSFFormatter {
    private static final Logger LOG = LoggerFactory.getLogger(RSFFormatter.class);

    private final String TAB = "\t";
    private final String NEW_LINE = System.lineSeparator();

    private final String ADD_ITEM_COMMAND_NAME = "add-item";
    private final String APPEND_ENTRY_COMMAND_NAME = "append-entry";
    private final String ASSERT_ROOT_HASH_COMMAND_NAME = "assert-root-hash";

    private final ObjectReconstructor objectReconstructor;
    private final CanonicalJsonMapper canonicalJsonMapper;
    private final CanonicalJsonValidator canonicalJsonValidator;

    private Integer position;
    private final HashMap<Integer, Item> items;
    private final HashMap<Integer, Entry> entries;
    private final HashMap<HashValue, Integer> itemHashToEntryCount;
    private final HashMap<Integer, RegisterProof> proofs;

    private final HashMap<String, Consumer<List<String>>> commandValidators;

    public RSFFormatter() {
        position = 0;
        objectReconstructor = new ObjectReconstructor();
        canonicalJsonMapper = new CanonicalJsonMapper();
        canonicalJsonValidator = new CanonicalJsonValidator();
        items = new HashMap<>();
        entries = new HashMap<>();
        itemHashToEntryCount = new HashMap<>();
        proofs = new HashMap<>();
        commandValidators = new HashMap<>();
        commandValidators.put(ADD_ITEM_COMMAND_NAME, getAddItemValidator());
        commandValidators.put(APPEND_ENTRY_COMMAND_NAME, getAppendEntryValidator());
        commandValidators.put(ASSERT_ROOT_HASH_COMMAND_NAME, getAssertRootHashValidator());
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

    public RegisterCommand2 parse(String str) throws SerializedRegisterParseException {
        List<String> parts = Arrays.asList(str.split(TAB));

        if (parts.isEmpty() || parts.size() < 2) {
            throw new IllegalArgumentException("String is empty or is in incorrect format");
        }

        String commandName = parts.get(0);
        List<String> commandParameters = parts.subList(1, parts.size());

        if (commandValidators.containsKey(commandName)) {
            commandValidators.get(commandName).accept(commandParameters);
        } else {
            throw new SerializedRegisterParseException("Command " + commandName + " is not recognised");
        }

        return new RegisterCommand2(commandName, commandParameters);
    }

    private Consumer<List<String>> getAddItemValidator() {
        return (arguments) -> {
            if (arguments.size() == 1) {
                String jsonContent = arguments.get(0);
                canonicalJsonValidator.validateItemStringIsCanonicalized(jsonContent);
            } else {
                LOG.error("add item line must have 1 argument, was: " + arguments);
                throw new SerializedRegisterParseException("Add item line must have 1 argument, was: " + arguments);
            }
        };
    }

    private Consumer<List<String>> getAppendEntryValidator() {
        return (arguments) -> {
            if (arguments.size() != 3) {
                LOG.error("append entry line must have 3 arguments, was : " + arguments);
                throw new SerializedRegisterParseException("Append entry line must have 3 arguments, was: " + arguments);
            }
        };
    }

    private Consumer<List<String>> getAssertRootHashValidator() {
        return (arguments) -> {
            if (arguments.size() == 1) {
                String sha256 = HashingAlgorithm.SHA256.toString();

                if (!arguments.get(0).startsWith(sha256)) {
                    LOG.error("Root hash value was not hashed using " + sha256);
                    throw new SerializedRegisterParseException("Root hash value was not hashed using " + sha256);
                }
            } else {
                LOG.error("Assert root hash line must have 1 arguments, was: " + arguments);
                throw new SerializedRegisterParseException("Assert root hash line must have 1 arguments, was: " + arguments);
            }
        };
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
