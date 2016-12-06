package uk.gov.register.db;

import org.glassfish.hk2.api.Factory;
import org.skife.jdbi.v2.DBI;
import uk.gov.register.core.EverythingAboutARegister;
import uk.gov.register.core.Register;

import javax.inject.Inject;

public abstract class Factories {
    private static abstract class SimpleFactory<T> implements Factory<T> {
        @Override
        public void dispose(T instance) {
            // do nothing
        }
    }
    public static class EntryQueryDAOFactory extends SimpleFactory<EntryQueryDAO> {
        private final DBI dbi;

        @Inject
        public EntryQueryDAOFactory(EverythingAboutARegister aboutARegister) {
            this.dbi = aboutARegister.getDbi();
        }

        @Override
        public EntryQueryDAO provide() {
            return dbi.onDemand(EntryQueryDAO.class);
        }
    }
    public static class ItemQueryDAOFactory extends SimpleFactory<ItemQueryDAO> {
        private final DBI dbi;
        @Inject
        public ItemQueryDAOFactory(EverythingAboutARegister aboutARegister) {
            this.dbi = aboutARegister.getDbi();
        }

        @Override
        public ItemQueryDAO provide() {
            return dbi.onDemand(ItemQueryDAO.class);
        }

    }
    public static class RecordQueryDAOFactory extends SimpleFactory<RecordQueryDAO> {
        private final DBI dbi;

        @Inject
        public RecordQueryDAOFactory(EverythingAboutARegister aboutARegister) {
            this.dbi = aboutARegister.getDbi();
        }

        @Override
        public RecordQueryDAO provide() {
            return dbi.onDemand(RecordQueryDAO.class);
        }
    }

    public static class PostgresRegisterFactory extends SimpleFactory<Register> {
        private final EverythingAboutARegister aboutARegister;

        @Inject
        public PostgresRegisterFactory(EverythingAboutARegister aboutARegister) {
            this.aboutARegister = aboutARegister;
        }

        @Override
        public Register provide() {
            return aboutARegister.buildOnDemandRegister();
        }
    }
}
