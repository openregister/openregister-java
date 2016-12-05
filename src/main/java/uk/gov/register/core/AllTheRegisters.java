package uk.gov.register.core;

import java.util.Map;
import java.util.stream.Stream;

public class AllTheRegisters {
    private EverythingAboutARegister defaultRegister;
    private final Map<String, EverythingAboutARegister> otherRegisters;

    public AllTheRegisters(EverythingAboutARegister defaultRegister, Map<String, EverythingAboutARegister> otherRegisters) {
        this.defaultRegister = defaultRegister;
        this.otherRegisters = otherRegisters;
    }

    public EverythingAboutARegister getRegisterByName(String name) {
        return otherRegisters.getOrDefault(name, defaultRegister);
    }

    public Stream<EverythingAboutARegister> stream() {
        return Stream.concat(Stream.of(defaultRegister),otherRegisters.values().stream());
    }
}
