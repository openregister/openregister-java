package uk.gov.register.serialization;

import uk.gov.register.core.Register;
import uk.gov.register.exceptions.RSFParseException;

public abstract class RegisterCommandHandler {
    protected abstract void executeCommand(RegisterCommand command, Register register);

    public abstract String getCommandName();

    public void execute(RegisterCommand command, Register register) {
        if (command.getCommandName().equals(getCommandName())) {
            executeCommand(command, register);
        } else {
            throw new RSFParseException("Incompatible handler (" + getCommandName() + ") and command type (" + command.getCommandName() + ")");
        }
    }
}

