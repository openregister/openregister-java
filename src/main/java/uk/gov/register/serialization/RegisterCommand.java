package uk.gov.register.serialization;

import uk.gov.register.core.Register;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class RegisterCommand {

    public abstract void execute(Register register, AtomicInteger entryNumber) throws Exception;

    public abstract String serialise(CommandParser commandParser);

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();
}

