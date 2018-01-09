package uk.gov.register.serialization;

import java.util.List;

public class RegisterCommand {
    private final String original;
    private final String commandName;
    private final List<String> arguments;

    public RegisterCommand(String commandName, List<String> arguments) {
        this.commandName = commandName;
        this.arguments = arguments;
        this.original = null;
    }

    public RegisterCommand(final String commandName, final List<String> arguments, final String original) {
        this.commandName = commandName;
        this.arguments = arguments;
        this.original = original;
    }

    public String getOriginal() {
        return original;
    }

    public String getCommandName() {
        return commandName;
    }

    public List<String> getCommandArguments() {
        return arguments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegisterCommand that = (RegisterCommand) o;

        if (!commandName.equals(that.commandName)) return false;
        return arguments.equals(that.arguments);
    }

    @Override
    public int hashCode() {
        int result = commandName.hashCode();
        result = 31 * result + arguments.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "RegisterCommand{" +
                "commandName='" + commandName + '\'' +
                ", arguments=" + arguments +
                '}';
    }
}
