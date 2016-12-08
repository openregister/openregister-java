package uk.gov.register.db;

import org.glassfish.hk2.api.Factory;
import uk.gov.register.core.Register;
import uk.gov.register.core.RegisterContext;

import javax.inject.Inject;

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
}
