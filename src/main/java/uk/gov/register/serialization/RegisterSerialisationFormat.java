package uk.gov.register.serialization;

import java.util.Iterator;

public class RegisterSerialisationFormat {
    public enum Version {V1, V2};
    private final Version version;
    private Iterator<RegisterCommand> commands;

    public RegisterSerialisationFormat(Version version, Iterator<RegisterCommand> commands) {
        this.commands = commands;
        this.version = version;
    }

    public Iterator<RegisterCommand> getCommands() {
        return commands;
    }

    public Version getVersion() {
        return version;
    }
}
