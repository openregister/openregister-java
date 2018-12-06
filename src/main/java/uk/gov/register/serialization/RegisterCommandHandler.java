package uk.gov.register.serialization;

import uk.gov.register.core.Register;
import uk.gov.register.core.RegisterContext;
import uk.gov.register.exceptions.RSFParseException;
import uk.gov.register.proofs.ProofGenerator;

public abstract class RegisterCommandHandler {
    protected abstract void executeCommand(RegisterCommand command, Register register, RegisterCommandContext registerContext);

    public abstract String getCommandName();

    public void execute(RegisterCommand command, Register register, RegisterCommandContext context) {
        if (command.getCommandName().equals(getCommandName())) {
            executeCommand(command, register, context);
        } else {
            throw new RSFParseException("Incompatible handler (" + getCommandName() + ") and command type (" + command.getCommandName() + ")");
        }
    }
}

