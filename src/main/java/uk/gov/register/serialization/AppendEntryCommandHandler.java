package uk.gov.register.serialization;

import uk.gov.register.core.Entry;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.core.Register;
import uk.gov.register.util.HashValue;
import uk.gov.register.util.ObjectReconstructor;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AppendEntryCommandHandler extends CommandHandler {


    @Override
    protected List<Exception> executeCommand(RegisterCommand2 command, Register register) {
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
