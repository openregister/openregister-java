package uk.gov.register.db;

import org.glassfish.hk2.api.Factory;
import uk.gov.register.core.AllTheRegisters;
import uk.gov.register.core.Register;
import uk.gov.register.core.RegisterContext;
import uk.gov.register.core.RegisterId;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

public abstract class Factories {
    private static abstract class SimpleFactory<T> implements Factory<T> {
        @Override
        public void dispose(T instance) {
            // do nothing
        }
    }

    public static class PostgresRegisterFactory extends SimpleFactory<Register> {
        private final RegisterContext registerContext;

        @Inject
        public PostgresRegisterFactory(RegisterContext registerContext) {
            this.registerContext = registerContext;
        }

        @Override
        public Register provide() {
            return registerContext.buildOnDemandRegister();
        }
    }

    public static class RegisterIdProvider extends SimpleFactory<RegisterId> {
        private final RegisterContext registerContext;

        @Inject
        public RegisterIdProvider(RegisterContext registerContext) {
            this.registerContext = registerContext;
        }

        @Override
        public RegisterId provide() {
            return registerContext.getRegisterId();
        }
    }

    public static class RegisterContextProvider extends SimpleFactory<RegisterContext> {
        private final AllTheRegisters allTheRegisters;
        private final Provider<HttpServletRequest> requestProvider;

        @Inject
        public RegisterContextProvider(AllTheRegisters allTheRegisters,
                                       Provider<HttpServletRequest> requestProvider) {
            this.allTheRegisters = allTheRegisters;
            this.requestProvider = requestProvider;
        }

        @Override
        public RegisterContext provide() {
            String host = getHost();
            String register = host.split("\\.")[0];
            return allTheRegisters.getRegisterById(new RegisterId(register));
        }

        private String getHost() {
            return firstNonNull(requestProvider.get().getHeader("X-Forwarded-Host"),
                    requestProvider.get().getHeader("Host"));
        }
    }
}
