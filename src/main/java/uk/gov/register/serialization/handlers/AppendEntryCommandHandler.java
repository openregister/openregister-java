package uk.gov.register.serialization.handlers;

import uk.gov.register.core.Entry;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Register;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterCommandHandler;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class AppendEntryCommandHandler extends RegisterCommandHandler {


    @Override
    protected List<Exception> executeCommand(RegisterCommand command, Register register) {
        try {
            List<String> parts = command.getCommandArguments();
            int newEntryNo = register.getTotalEntries() + 1;
            Entry entry = new Entry(newEntryNo, HashValue.decode(HashingAlgorithm.SHA256, parts.get(1)), Instant.parse(parts.get(0)), parts.get(2));
            register.appendEntry(entry);
            return Collections.emptyList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.singletonList(e);
        }
    }

    @Override
    public String getCommandName() {
        return "append-entry";
    }
}
