package uk.gov.register.serialization;

import uk.gov.register.core.Register;

import java.util.Arrays;
import java.util.List;

public abstract class RegisterCommandHandler {
    protected abstract List<Exception> executeCommand(RegisterCommand command, Register register);
    public abstract String getCommandName();

    public List<Exception> execute(RegisterCommand command, Register register){
        if (command.getCommandName().equals(getCommandName())){

            return executeCommand(command, register);
        } else {
            return Arrays.asList(new Exception("Incompatible handler and command type"));
        }
    }


}

