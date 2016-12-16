package uk.gov.register.core;

import java.util.Map;
import java.util.stream.Stream;

public class AllTheRegisters {
    private RegisterContext defaultRegister;
    private final Map<String, RegisterContext> otherRegisters;

    public AllTheRegisters(RegisterContext defaultRegister, Map<String, RegisterContext> otherRegisters) {
        this.defaultRegister = defaultRegister;
        this.otherRegisters = otherRegisters;
    }

    public RegisterContext getRegisterByName(String name) {
        return otherRegisters.getOrDefault(name, defaultRegister);
    }

    public Stream<RegisterContext> stream() {
        return Stream.concat(Stream.of(defaultRegister),otherRegisters.values().stream());
    }
}
