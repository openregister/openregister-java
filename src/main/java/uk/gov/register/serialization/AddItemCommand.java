package uk.gov.register.serialization;

import uk.gov.register.core.Item;
import uk.gov.register.core.Register;

public class AddItemCommand extends RegisterCommand {

    private Item item;

    public AddItemCommand(Item item) {
        this.item = item;
    }

    @Override
    public void execute(Register register) {
        register.putItem(item);
    }

    @Override
    public String serialise(CommandParser commandParser) {
        return commandParser.serialise(item);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AddItemCommand command = (AddItemCommand) o;
        return command.item == this.item;
    }

    @Override
    public int hashCode() {
        return 31 * item.hashCode();
    }
}
