package uk.gov.register.serialization;

import java.util.List;

public class RegisterCommandList {

    public final List<RegisterCommand> commands;

    public RegisterCommandList(List<RegisterCommand> commands) {
        this.commands = commands;
    }
}
