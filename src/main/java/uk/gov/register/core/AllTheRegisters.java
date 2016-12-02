package uk.gov.register.core;

public class AllTheRegisters {
    private EverythingAboutARegister defaultRegister;

    public AllTheRegisters(EverythingAboutARegister defaultRegister) {
        this.defaultRegister = defaultRegister;
    }

    public EverythingAboutARegister getRegisterByName(String name) {
        return defaultRegister;
    }

}
