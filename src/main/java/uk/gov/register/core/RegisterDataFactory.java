package uk.gov.register.core;

import org.glassfish.hk2.api.Factory;

import javax.inject.Inject;

public class RegisterDataFactory implements Factory<RegisterData> {
    private final EverythingAboutARegister everythingAboutARegister;

    @Inject
    public RegisterDataFactory(EverythingAboutARegister everythingAboutARegister) {
        this.everythingAboutARegister = everythingAboutARegister;
    }

    @Override
    public RegisterData provide() {
        return everythingAboutARegister.getRegisterData();
    }

    @Override
    public void dispose(RegisterData instance) {

    }
}
