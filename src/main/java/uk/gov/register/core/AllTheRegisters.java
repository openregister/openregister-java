package uk.gov.register.core;

import java.util.Map;

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

}
