package uk.gov.register.serialization.handlers;

import uk.gov.register.core.Entry;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Register;
import uk.gov.register.serialization.RSFResult;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterCommandHandler;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.List;

public class AppendEntryCommandHandler extends RegisterCommandHandler {
    @Override
    protected RSFResult executeCommand(RegisterCommand command, Register register) {
        try {
            List<String> parts = command.getCommandArguments();
            int newEntryNo = register.getTotalEntries() + 1;
            Entry entry = new Entry(newEntryNo, HashValue.decode(HashingAlgorithm.SHA256, parts.get(1)), Instant.parse(parts.get(0)), parts.get(2));
            register.appendEntry(entry);
            return RSFResult.createSuccessResult();
        } catch (Exception e) {
            return RSFResult.createFailResult("Exception when executing command: " + command, e);
        }
    }

    @Override
    public String getCommandName() {
        return "append-entry";
    }
}
