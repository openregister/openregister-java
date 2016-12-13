package uk.gov.register.core;

import java.util.Map;
import java.util.stream.Stream;

public class AllTheRegisters {
    private RegisterContext defaultRegister;
    private final Map<RegisterName, RegisterContext> otherRegisters;

    public AllTheRegisters(RegisterContext defaultRegister, Map<RegisterName, RegisterContext> otherRegisters) {
        this.defaultRegister = defaultRegister;
        this.otherRegisters = otherRegisters;
    }

    public RegisterContext getRegisterByName(RegisterName name) {
        return otherRegisters.getOrDefault(name, defaultRegister);
    }

    public Stream<RegisterContext> stream() {
        return Stream.concat(Stream.of(defaultRegister),otherRegisters.values().stream());
    }
}
