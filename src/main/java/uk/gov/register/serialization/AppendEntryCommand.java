package uk.gov.register.serialization;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Register;

public class AppendEntryCommand extends RegisterCommand {

    private Entry entry;

    public AppendEntryCommand(Entry entry) {
        this.entry = entry;
    }

    @Override
    public void execute(Register register) {
        int nextEntryNumber = register.getTotalEntries() + 1;
        Entry numberedEntry = new Entry(nextEntryNumber, entry.getSha256hex(), entry.getTimestamp(), entry.getItemKey());
        register.appendEntry(numberedEntry);
    }

    @Override
    public String serialise(CommandParser parser) {
        return parser.serialise(entry);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppendEntryCommand command = (AppendEntryCommand) o;
        return command.entry == this.entry;
    }

    @Override
    public int hashCode() {
        return 31 * entry.hashCode();
    }
}
