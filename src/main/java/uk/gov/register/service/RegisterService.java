package uk.gov.register.service;

import org.skife.jdbi.v2.DBI;
import uk.gov.register.configuration.RegisterFieldsConfiguration;
import uk.gov.register.core.EverythingAboutARegister;
import uk.gov.register.core.PostgresRegister;
import uk.gov.register.core.Register;
import uk.gov.register.core.RegisterData;
import uk.gov.register.store.postgres.PostgresDriverTransactional;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import javax.inject.Inject;
import java.util.function.Consumer;

public class RegisterService {
    private final RegisterData registerData;
    private final DBI dbi;
    private final MemoizationStore memoizationStore;
    private final ItemValidator itemValidator;
    private final RegisterFieldsConfiguration registerFieldsConfiguration;

    @Inject
    public RegisterService(ItemValidator itemValidator, EverythingAboutARegister everythingAboutARegister) {
        this.registerData = everythingAboutARegister.getRegisterData();
        this.dbi = everythingAboutARegister.getDbi();
        this.memoizationStore = everythingAboutARegister.getMemoizationStore();
        this.itemValidator = itemValidator;
        this.registerFieldsConfiguration = everythingAboutARegister.getFieldsConfiguration();
    }

    public void asAtomicRegisterOperation(Consumer<Register> callback) {
        PostgresDriverTransactional.useTransaction(dbi, memoizationStore, postgresDriver -> {
            Register register = new PostgresRegister(registerData, postgresDriver, itemValidator, registerFieldsConfiguration);
            callback.accept(register);
        });
    }
}
