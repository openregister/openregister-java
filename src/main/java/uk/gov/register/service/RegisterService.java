package uk.gov.register.service;

import org.skife.jdbi.v2.DBI;
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

    @Inject
    public RegisterService(RegisterData registerData, DBI dbi, MemoizationStore memoizationStore, ItemValidator itemValidator) {
        this.registerData = registerData;
        this.dbi = dbi;
        this.memoizationStore = memoizationStore;
        this.itemValidator = itemValidator;
    }

    public void asAtomicRegisterOperation(Consumer<Register> callback) {
        PostgresDriverTransactional.useTransaction(dbi, memoizationStore, postgresDriver -> {
            Register register = new PostgresRegister(registerData, postgresDriver, itemValidator);
            callback.accept(register);
        });
    }
}