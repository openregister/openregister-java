package uk.gov.register.serialization;

import uk.gov.register.core.Register;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class RegisterCommand {

    public abstract void execute(Register register, AtomicInteger entryNumber) throws Exception;

    public abstract String serialise(CommandParser commandParser);

}

