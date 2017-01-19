package uk.gov.register.serialization;

import uk.gov.register.core.Register;

public abstract class RegisterCommandHandler {
    protected abstract RegisterResult executeCommand(RegisterCommand command, Register register);

    public abstract String getCommandName();

    public RegisterResult execute(RegisterCommand command, Register register) {
        if (command.getCommandName().equals(getCommandName())) {
            return executeCommand(command, register);
        } else {
            return RegisterResult.createFailResult("Incompatible handler (" + getCommandName() + ") and command type (" + command.getCommandName() + ")");
        }
    }
}

