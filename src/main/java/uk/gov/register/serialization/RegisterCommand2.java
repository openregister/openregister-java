package uk.gov.register.serialization;

import java.util.List;

public class RegisterCommand2 {
    private final String commandName;
    private final List<String> arguments;

    public RegisterCommand2(String commandName, List<String> arguments) {
        this.commandName = commandName;
        this.arguments = arguments;
    }

    public String getCommandName() {
        return commandName;
    }

    public List<String> getCommandArguments() {
        return arguments;
    }
}
