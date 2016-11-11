package uk.gov.register.serialization;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Register;

import java.util.concurrent.atomic.AtomicInteger;

public class AppendEntryCommand implements RegisterCommand {

    private Entry entry;

    public AppendEntryCommand(Entry entry) {
        this.entry = entry;
    }

    @Override
    public void execute(Register register, AtomicInteger entryNumber) {
        Entry numberedEntry = new Entry(entryNumber.getAndIncrement(), entry.getSha256hex(), entry.getTimestamp());
        register.appendEntry(numberedEntry);
    }

    @Override
    public String serialise(CommandParser parser) {
        return parser.serialise(entry);
    }


}
