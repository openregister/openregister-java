package uk.gov.register.serialization.handlers;

import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Register;
import uk.gov.register.exceptions.RSFParseException;
import uk.gov.register.serialization.RSFFormatter;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterCommandHandler;
import uk.gov.register.serialization.RegisterResult;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.register.core.HashingAlgorithm.SHA256;

public class AppendEntryCommandHandler extends RegisterCommandHandler {
    @Override
    protected void executeCommand(RegisterCommand command, Register register) {
        try {
            List<String> parts = command.getCommandArguments();
            String delimitedHashes = parts.get(RSFFormatter.RSF_HASH_POSITION);
            List<HashValue> hashValues;
            if (StringUtils.isNotEmpty(delimitedHashes)) {
                hashValues = Splitter.on(";").splitToList(delimitedHashes).stream()
                        .map(h -> HashValue.decode(SHA256, h)).collect(toList());
            } else {
                hashValues = new ArrayList<>();
            }
            EntryType entryType = EntryType.valueOf(parts.get(RSFFormatter.RSF_ENTRY_TYPE_POSITION));
            int newEntryNo = register.getTotalEntries(entryType) + 1;
            Entry entry = new Entry(newEntryNo, hashValues, Instant.parse(parts.get(RSFFormatter.RSF_TIMESTAMP_POSITION)), parts.get(RSFFormatter.RSF_KEY_POSITION), entryType);
            register.appendEntry(entry);
        } catch (Exception e) {
            throw new RSFParseException("Exception when executing command: " + command, e);
        }
    }

    @Override
    public String getCommandName() {
        return "append-entry";
    }
}
