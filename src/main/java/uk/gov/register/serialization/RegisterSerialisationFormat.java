package uk.gov.register.serialization;

import java.util.List;

public class RegisterSerialisationFormat {
    private List<RegisterCommand> commands;

    public RegisterSerialisationFormat(List<RegisterCommand> commands) {
        this.commands = commands;
    }

    public List<RegisterCommand> getCommands() {
        return commands;
    }
}
