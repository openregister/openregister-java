package uk.gov.register.serialization.handlers;

import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Register;
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
    protected RegisterResult executeCommand(RegisterCommand command, Register register) {
        try {
            List<String> parts = command.getCommandArguments();
            int newEntryNo = register.getTotalEntries() + 1;
            String delimitedHashes = parts.get(2);
            List<HashValue> hashValues;
            if (StringUtils.isNotEmpty(delimitedHashes)) {
                hashValues = Splitter.on(";").splitToList(delimitedHashes).stream()
                        .map(h -> HashValue.decode(SHA256, h)).collect(toList());
            } else {
                hashValues = new ArrayList<>();
            }
            Entry entry = new Entry(newEntryNo, hashValues, Instant.parse(parts.get(1)), parts.get(0), EntryType.user);
            register.appendEntry(entry);
            return RegisterResult.createSuccessResult();
        } catch (Exception e) {
            return RegisterResult.createFailResult("Exception when executing command: " + command, e);
        }
    }

    @Override
    public String getCommandName() {
        return "append-entry";
    }
}
