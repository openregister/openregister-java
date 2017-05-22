package uk.gov.register.serialization;

import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.exceptions.SerializationFormatValidationException;
import uk.gov.register.exceptions.SerializedRegisterParseException;
import uk.gov.register.util.CanonicalJsonValidator;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class RSFFormatter {
    private static final String SHA_256 = HashingAlgorithm.SHA256.toString();

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

    public RegisterCommand parse(String str) throws SerializedRegisterParseException {
        // -1 -> if str ends with \t then final string in list will be ""
        List<String> parts = Arrays.asList(str.split(TAB, -1));

        if (parts.isEmpty() || parts.size() < 2) {
            throw new SerializedRegisterParseException("String is empty or is in incorrect format");
        }

        String commandName = parts.get(0);
        List<String> commandParameters = parts.subList(1, parts.size());

        if (commandValidators.containsKey(commandName)) {
            commandValidators.get(commandName).accept(commandParameters);
        } else {
            throw new SerializedRegisterParseException("Command " + commandName + " is not recognised");
        }

        return new RegisterCommand(commandName, commandParameters);
    }

    private Consumer<List<String>> getAddItemValidator() {
        return (arguments) -> {
            if (arguments.size() == 1) {
                String jsonContent = arguments.get(0);
                try {
                    canonicalJsonValidator.validateItemStringIsCanonicalized(jsonContent);
                } catch (SerializationFormatValidationException e) {
                    throw new SerializedRegisterParseException("Non canonical JSON: " + e.getMessage(), e);
                } catch (Exception e) {
                    throw new SerializedRegisterParseException("Invalid JSON: " + e.getMessage(), e);
                }
            } else {
                throw new SerializedRegisterParseException("Add item line must have 1 argument, was: " + argsToString(arguments));
            }
        };
    }

    private Consumer<List<String>> getAppendEntryValidator() {
        return (arguments) -> {
            if (arguments.size() != 4) {
                throw new SerializedRegisterParseException("Append entry line must have 4 arguments, was: " + argsToString(arguments));
            }
            try {
                EntryType entryType = EntryType.valueOf(arguments.get(0));
                Instant.parse(arguments.get(2));
            } catch (DateTimeParseException e) {
                throw new SerializedRegisterParseException("Date is not in the correct format", e);
            } catch (IllegalArgumentException iae){
                throw new SerializedRegisterParseException("Type must be 'user' or 'system'", iae);
            }
            validateHash(arguments.get(3), "Append entry hash value was not hashed using " + SHA_256);
        };
    }

    private Consumer<List<String>> getAssertRootHashValidator() {
        return (arguments) -> {
            if (arguments.size() == 1) {
                validateHash(arguments.get(0), "Root hash value was not hashed using " + SHA_256);
            } else {
                throw new SerializedRegisterParseException("Assert root hash line must have 1 arguments, was: " + argsToString(arguments));
            }
        };
    }

    private void validateHash(String hash, String errorMessage) {
        if ("true".equals(System.getProperty("multi-item-entries-enabled"))) {
            if (StringUtils.isNotEmpty(hash) && !allPartsHaveShaPrefix(hash)) {
                throw new SerializedRegisterParseException(errorMessage);
            }
        } else {
            if (!hash.startsWith(SHA_256) || hash.indexOf(';') != -1) {
                throw new SerializedRegisterParseException(errorMessage);
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
