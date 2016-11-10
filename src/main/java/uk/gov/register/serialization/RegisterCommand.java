package uk.gov.register.serialization;

import uk.gov.register.core.Register;

import java.util.concurrent.atomic.AtomicInteger;

public interface RegisterCommand {

    void execute(Register register, AtomicInteger entryNumber);

    String serialise(CommandParser commandParser);
}

