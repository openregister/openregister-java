package uk.gov.register.serialization;

import java.util.List;

public class RegisterComponents {

    public final List<RegisterCommand> commands;

    public RegisterComponents(List<RegisterCommand> commands) {
        this.commands = commands;
    }
}
