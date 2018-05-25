package uk.gov.register.serialization;

import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.exceptions.SerializationFormatValidationException;
import uk.gov.register.exceptions.RSFParseException;
import uk.gov.register.util.CanonicalJsonValidator;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class RSFFormatter {
    private static final String SHA_256 = HashingAlgorithm.SHA256.toString();

    public static final int RSF_ENTRY_TYPE_POSITION = 0;
    public static final int RSF_KEY_POSITION = 1;
    public static final int RSF_TIMESTAMP_POSITION = 2;
    public static final int RSF_HASH_POSITION = 3;
    public static final int APPEND_ENTRY_ARGUMENT_COUNT = 4;
    public static final int ASSERT_ROOT_HASH_ARGUMENT_COUNT = 1;
    public static final int ADD_ITEM_ARGUMENT_COUNT = 1;
    public static final int RSF_ITEM_ARGUMENT_POSITION = 0;
    public static final int RSF_ASSERT_ROOT_HASH_ARGUMENT_POSITION = 0;

    private final String TAB = "\t";

    private final String NEW_LINE = System.lineSeparator();
    private final String ADD_ITEM_COMMAND_NAME = "add-item";
    private final String APPEND_ENTRY_COMMAND_NAME = "append-entry";
    private final String ASSERT_ROOT_HASH_COMMAND_NAME = "assert-root-hash";

    private final CanonicalJsonValidator canonicalJsonValidator;

    private final HashMap<String, Consumer<List<String>>> commandValidators;

    public RSFFormatter() {
        canonicalJsonValidator = new CanonicalJsonValidator();
        commandValidators = new HashMap<>();
        commandValidators.put(ADD_ITEM_COMMAND_NAME, getAddItemValidator());
        commandValidators.put(APPEND_ENTRY_COMMAND_NAME, getAppendEntryValidator());
        commandValidators.put(ASSERT_ROOT_HASH_COMMAND_NAME, getAssertRootHashValidator());
    }

    public String getFileExtension() {
        return "rsf";
    }

    public String format(RegisterCommand command) {
        return command.getCommandName() + TAB + String.join(TAB, command.getCommandArguments()) + NEW_LINE;
    }

    public RegisterCommand parse(String str) throws RSFParseException {
        // -1 -> if str ends with \t then final string in list will be ""
        List<String> parts = Arrays.asList(str.split(TAB, -1));

        if (parts.isEmpty() || parts.size() < 2) {
            throw new RSFParseException("String is empty or is in incorrect format");
        }

        String commandName = parts.get(0);
        List<String> commandParameters = parts.subList(1, parts.size());

        if (commandValidators.containsKey(commandName)) {
            commandValidators.get(commandName).accept(commandParameters);
        } else {
            throw new RSFParseException("Command " + commandName + " is not recognised");
        }

        return new RegisterCommand(commandName, commandParameters);
    }

    private Consumer<List<String>> getAddItemValidator() {
        return (arguments) -> {
            if (arguments.size() == ADD_ITEM_ARGUMENT_COUNT) {
                String jsonContent = arguments.get(RSF_ITEM_ARGUMENT_POSITION);
                try {
                    canonicalJsonValidator.validateItemStringIsCanonicalized(jsonContent);
                } catch (SerializationFormatValidationException e) {
                    throw new RSFParseException("Non canonical JSON", e);
                } catch (Exception e) {
                    throw new RSFParseException("Invalid JSON", e);
                }
            } else {
                throw new RSFParseException("Add item line must have " + ADD_ITEM_ARGUMENT_COUNT + " argument, was: " + argsToString(arguments));
            }
        };
    }

    private Consumer<List<String>> getAppendEntryValidator() {
        return (arguments) -> {
            if (arguments.size() != APPEND_ENTRY_ARGUMENT_COUNT) {
                throw new RSFParseException("Append entry line must have " + APPEND_ENTRY_ARGUMENT_COUNT + " arguments, was: " + argsToString(arguments));
            }
            try {
                EntryType.valueOf(arguments.get(RSF_ENTRY_TYPE_POSITION));
                Instant.parse(arguments.get(RSF_TIMESTAMP_POSITION));
            } catch (DateTimeParseException e) {
                throw new RSFParseException("Date is not in the correct format", e);
            } catch (IllegalArgumentException iae) {
                throw new RSFParseException("Type must be 'user' or 'system'", iae);
            }
            validateHash(arguments.get(RSF_HASH_POSITION), "Append entry hash value was not hashed using " + SHA_256);
        };
    }

    private Consumer<List<String>> getAssertRootHashValidator() {
        return (arguments) -> {
            if (arguments.size() == ASSERT_ROOT_HASH_ARGUMENT_COUNT) {
                validateHash(arguments.get(RSF_ASSERT_ROOT_HASH_ARGUMENT_POSITION), "Root hash value was not hashed using " + SHA_256);
            } else {
                throw new RSFParseException("Assert root hash line must have " + ASSERT_ROOT_HASH_ARGUMENT_COUNT + " arguments, was: " + argsToString(arguments));
            }
        };
    }

    private void validateHash(String hash, String errorMessage) {
        if ("true".equals(System.getProperty("multi-item-entries-enabled"))) {
            if (StringUtils.isNotEmpty(hash) && !allPartsHaveShaPrefix(hash)) {
                throw new RSFParseException(errorMessage);
            }
        } else {
            if (!hash.startsWith(SHA_256) || hash.indexOf(';') != -1) {
                throw new RSFParseException(errorMessage);
            }
        }
    }

    private boolean allPartsHaveShaPrefix(String hash) {
        return Splitter.on(";").splitToList(hash).stream().allMatch(s -> s.startsWith(SHA_256));
    }

    private String argsToString(List<String> arguments) {
        return String.join(", ", arguments);
    }
}
