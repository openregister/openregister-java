package uk.gov.register.serialization;

import uk.gov.register.core.Register;

public interface RegisterCommand {

    void execute(Register register);

    String serialise(CommandParser commandParser);
}

