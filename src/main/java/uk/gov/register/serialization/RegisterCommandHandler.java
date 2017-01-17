package uk.gov.register.serialization;

import uk.gov.register.core.Register;

public abstract class RegisterCommandHandler {
    protected abstract RSFResult executeCommand(RegisterCommand command, Register register);

    public abstract String getCommandName();

    public RSFResult execute(RegisterCommand command, Register register) {
        if (command.getCommandName().equals(getCommandName())) {
            return executeCommand(command, register);
        } else {
            return RSFResult.createFailResult("Incompatible handler (" + getCommandName() + ") and command type (" + command.getCommandName() + ")");
        }
    }
}

