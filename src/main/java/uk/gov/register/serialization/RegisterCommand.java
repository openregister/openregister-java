package uk.gov.register.serialization;

import uk.gov.register.core.Register;

public abstract class RegisterCommand {

    public abstract void execute(Register register) throws Exception;

    public abstract String serialise(CommandParser commandParser);

}

