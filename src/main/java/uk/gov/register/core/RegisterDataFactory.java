package uk.gov.register.core;

import org.glassfish.hk2.api.Factory;

import javax.inject.Inject;
import javax.inject.Provider;

public class RegisterDataFactory implements Factory<RegisterData> {
    private final Provider<EverythingAboutARegister> everythingAboutARegister;

    @Inject
    public RegisterDataFactory(Provider<EverythingAboutARegister> everythingAboutARegister) {
        this.everythingAboutARegister = everythingAboutARegister;
    }

    @Override
    public RegisterData provide() {
        return everythingAboutARegister.get().getRegisterData();
    }

    @Override
    public void dispose(RegisterData instance) {

    }
}
