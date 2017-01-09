package uk.gov.register.serialization;

import java.util.Iterator;

public class RegisterSerialisationFormat {

    private  Iterator<RegisterCommand> commands;
    private  Iterator<RegisterCommand2> commands2;

    public RegisterSerialisationFormat(Iterator<RegisterCommand> commands) {
        this.commands = commands;
    }

    public RegisterSerialisationFormat(Iterator<RegisterCommand> commands, Iterator<RegisterCommand2> commands2) {
        this.commands = commands;
        this.commands2 = commands2;
    }

    public Iterator<RegisterCommand> getCommands() {
        return commands;
    }
    public Iterator<RegisterCommand2> getCommands2() {
        return commands2;
    }
}
