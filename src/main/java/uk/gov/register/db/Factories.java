package uk.gov.register.db;

import org.glassfish.hk2.api.Factory;
import uk.gov.register.configuration.RegisterFieldsConfiguration;
import uk.gov.register.core.AllTheRegisters;
import uk.gov.register.core.Register;
import uk.gov.register.core.RegisterContext;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.resources.RequestContext;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.google.common.collect.Lists.newArrayList;

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

    public static class RegisterContextProvider extends SimpleFactory<RegisterContext> {
        private final AllTheRegisters allTheRegisters;
        private final Provider<RequestContext> requestContext;

        @Inject
        public RegisterContextProvider(AllTheRegisters allTheRegisters,
                                       Provider<RequestContext> requestContext) {
            this.allTheRegisters = allTheRegisters;
            this.requestContext = requestContext;
        }

        @Override
        public RegisterContext provide() {
            String host = requestContext.get().getHost();
            String register = host.split("\\.")[0];
            return allTheRegisters.getRegisterByName(register);
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
