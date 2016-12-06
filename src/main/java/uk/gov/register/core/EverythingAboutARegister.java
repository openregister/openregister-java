package uk.gov.register.core;

import org.flywaydb.core.Flyway;
import org.skife.jdbi.v2.DBI;
import uk.gov.register.configuration.RegisterFieldsConfiguration;
import uk.gov.register.configuration.RegistersConfiguration;
import uk.gov.register.db.*;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

public class EverythingAboutARegister {
    private String registerName;
    private RegistersConfiguration registersConfiguration;
    private MemoizationStore memoizationStore;
    private DBI dbi;
    private Flyway flyway;

    public EverythingAboutARegister(String registerName, RegistersConfiguration registersConfiguration, MemoizationStore memoizationStore, DBI dbi, Flyway flyway) {
        this.registerName = registerName;
        this.registersConfiguration = registersConfiguration;
        this.memoizationStore = memoizationStore;
        this.dbi = dbi;
        this.flyway = flyway;
    }

    public String getRegisterName() {
        return registerName;
    }

    public RegisterFieldsConfiguration getRegisterFieldsConfiguration() {
        return new RegisterFieldsConfiguration(getRegisterData().getRegister().getFields());
    }

    public RegisterData getRegisterData() {
        return registersConfiguration.getRegisterData(getRegisterName());
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

    public Register getOnDemandRegister() {
        DBI dbi = getDbi();
        EntryQueryDAO entryQueryDAO = dbi.onDemand(EntryQueryDAO.class);
        ItemQueryDAO itemQueryDAO = dbi.onDemand(ItemQueryDAO.class);
        RecordQueryDAO recordQueryDAO = dbi.onDemand(RecordQueryDAO.class);
        return new PostgresRegister(getRegisterData(),
                getRegisterFieldsConfiguration(),
                new UnmodifiableEntryLog(memoizationStore, entryQueryDAO),
                new UnmodifiableItemStore(itemQueryDAO),
                new UnmodifiableRecordIndex(recordQueryDAO)
        );
    }
}
