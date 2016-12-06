package uk.gov.register.core;

import org.flywaydb.core.Flyway;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import uk.gov.register.configuration.FieldsConfiguration;
import uk.gov.register.configuration.RegisterFieldsConfiguration;
import uk.gov.register.configuration.RegistersConfiguration;
import uk.gov.register.db.*;
import uk.gov.register.service.ItemValidator;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

public class EverythingAboutARegister {
    private String registerName;
    private RegistersConfiguration registersConfiguration;
    private FieldsConfiguration fieldsConfiguration;
    private MemoizationStore memoizationStore;
    private DBI dbi;
    private Flyway flyway;

    public EverythingAboutARegister(String registerName, RegistersConfiguration registersConfiguration, FieldsConfiguration fieldsConfiguration, MemoizationStore memoizationStore, DBI dbi, Flyway flyway) {
        this.registerName = registerName;
        this.registersConfiguration = registersConfiguration;
        this.fieldsConfiguration = fieldsConfiguration;
        this.memoizationStore = memoizationStore;
        this.dbi = dbi;
        this.flyway = flyway;
    }

    public RegisterFieldsConfiguration getRegisterFieldsConfiguration() {
        return new RegisterFieldsConfiguration(getRegisterData().getRegister().getFields());
    }

    public RegisterData getRegisterData() {
        return registersConfiguration.getRegisterData(registerName);
    }

    public MemoizationStore getMemoizationStore() {
        return memoizationStore;
    }

    public DBI getDbi() {
        return dbi;
    }

    public Flyway getFlyway() {
        return flyway;
    }

    public Register buildOnDemandRegister() {
        return new PostgresRegister(getRegisterData(),
                getRegisterFieldsConfiguration(),
                new UnmodifiableEntryLog(memoizationStore, getDbi().onDemand(EntryQueryDAO.class)),
                new UnmodifiableItemStore(getDbi().onDemand(ItemQueryDAO.class)),
                new UnmodifiableRecordIndex(getDbi().onDemand(RecordQueryDAO.class))
        );
    }

    public Register buildTransactionalRegister(Handle handle, TransactionalMemoizationStore memoizationStore) {
        return new PostgresRegister(getRegisterData(),
                getRegisterFieldsConfiguration(),
                new TransactionalEntryLog(memoizationStore,
                        handle.attach(EntryQueryDAO.class),
                        handle.attach(EntryDAO.class)),
                new TransactionalItemStore(
                        handle.attach(ItemDAO.class),
                        handle.attach(ItemQueryDAO.class),
                        new ItemValidator(registersConfiguration, fieldsConfiguration, registerName)),
                new TransactionalRecordIndex(
                        handle.attach(RecordQueryDAO.class),
                        handle.attach(CurrentKeysUpdateDAO.class)
                )
        );
    }
}
