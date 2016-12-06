package uk.gov.register.core;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.PerLookup;
import uk.gov.register.resources.RequestContext;

import javax.inject.Inject;
import javax.inject.Provider;

public class EverythingAboutARegisterProvider implements Factory<EverythingAboutARegister> {
    private final AllTheRegisters allTheRegisters;
    private final Provider<RequestContext> requestContext;

    @Inject
    public EverythingAboutARegisterProvider(AllTheRegisters allTheRegisters,
                                            Provider<RequestContext> requestContext) {
        this.allTheRegisters = allTheRegisters;
        this.requestContext = requestContext;
    }

    @Override
    @PerLookup
    public EverythingAboutARegister provide() {
        String host = requestContext.get().getHost();
        String register = host.split("\\.")[0];
        return allTheRegisters.getRegisterByName(register);
    }

    @Override
    public void dispose(EverythingAboutARegister instance) {

    }
}
