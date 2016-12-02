package uk.gov.register.core;

import org.glassfish.hk2.api.Factory;
import uk.gov.register.configuration.RegisterNameConfiguration;

import javax.inject.Inject;

public class EverythingAboutARegisterProvider implements Factory<EverythingAboutARegister> {
    private final RegisterNameConfiguration registerNameConfiguration;
    private final AllTheRegisters allTheRegisters;

    @Inject
    public EverythingAboutARegisterProvider(RegisterNameConfiguration registerNameConfiguration,
                                            AllTheRegisters allTheRegisters) {
        this.registerNameConfiguration = registerNameConfiguration;
        this.allTheRegisters = allTheRegisters;
    }

    @Override
    public EverythingAboutARegister provide() {
        return allTheRegisters.getRegisterByName(registerNameConfiguration.getRegisterName());
    }

    @Override
    public void dispose(EverythingAboutARegister instance) {

    }
}
