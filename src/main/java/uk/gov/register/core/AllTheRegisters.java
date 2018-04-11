package uk.gov.register.core;

import java.util.Map;
import java.util.stream.Stream;

public class AllTheRegisters {
    private RegisterContext defaultRegister;
    private final Map<RegisterId, RegisterContext> otherRegisters;

    public AllTheRegisters(RegisterContext defaultRegister, Map<RegisterId, RegisterContext> otherRegisters) {
        this.defaultRegister = defaultRegister;
        this.otherRegisters = otherRegisters;
    }

    public RegisterContext getRegisterById(RegisterId id) {
        return otherRegisters.getOrDefault(id, defaultRegister);
    }

    public Stream<RegisterContext> stream() {
        return Stream.concat(Stream.of(defaultRegister),otherRegisters.values().stream());
    }
}
