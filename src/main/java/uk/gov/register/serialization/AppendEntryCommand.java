package uk.gov.register.serialization;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Register;

public class AppendEntryCommand implements RegisterCommand {

    private Entry entry;

    public AppendEntryCommand(Entry entry) {
        this.entry = entry;
    }

    @Override
    public void execute(Register register) {
        register.appendEntry(entry);
    }

    @Override
    public String serialise(CommandParser parser) {
        return parser.serialise(entry);
    }


}
