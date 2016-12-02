package uk.gov.register.core;

import org.flywaydb.core.Flyway;
import org.skife.jdbi.v2.DBI;
import uk.gov.register.configuration.RegisterFieldsConfiguration;
import uk.gov.register.configuration.RegistersConfiguration;
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

    public RegisterFieldsConfiguration getFieldsConfiguration() {
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
}
