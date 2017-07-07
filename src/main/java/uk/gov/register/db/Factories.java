package uk.gov.register.db;

import org.glassfish.hk2.api.Factory;
import uk.gov.register.configuration.RegisterFieldsConfiguration;
import uk.gov.register.core.*;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

import static com.google.common.collect.Lists.newArrayList;
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

    public static class RegisterNameProvider extends SimpleFactory<RegisterName> {
        private final RegisterContext registerContext;

        @Inject
        public RegisterNameProvider(RegisterContext registerContext) {
            this.registerContext = registerContext;
        }

        @Override
        public RegisterName provide() {
            return registerContext.getRegisterName();
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
            return allTheRegisters.getRegisterByName(new RegisterName(register));
        }

        private String getHost() {
            return firstNonNull(requestProvider.get().getHeader("X-Forwarded-Host"),
                    requestProvider.get().getHeader("Host"));
        }
    }

    public static class RegisterMetadataFactory extends SimpleFactory<RegisterMetadata> {
        private final Provider<RegisterContext> registerContextProvider;

        @Inject
        public RegisterMetadataFactory(Provider<RegisterContext> registerContextProvider) {
            this.registerContextProvider = registerContextProvider;
        }

        @Override
        public RegisterMetadata provide() {
            return registerContextProvider.get().getRegisterMetadata();
        }
    }

    public static class RegisterFieldsConfigurationFactory extends SimpleFactory<RegisterFieldsConfiguration> {
        private final RegisterMetadata registerMetadata;

        @Inject
        public RegisterFieldsConfigurationFactory(RegisterMetadata registerMetadata) {
            this.registerMetadata = registerMetadata;
        }

        @Override
        public RegisterFieldsConfiguration provide() {
            return new RegisterFieldsConfiguration(newArrayList(registerMetadata.getFields()));
        }
    }
}
