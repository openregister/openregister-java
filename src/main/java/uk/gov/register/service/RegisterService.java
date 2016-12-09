package uk.gov.register.service;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import uk.gov.register.configuration.RegisterFieldsConfiguration;
import uk.gov.register.core.PostgresRegister;
import uk.gov.register.core.Register;
import uk.gov.register.core.RegisterData;
import uk.gov.register.core.TransactionalMemoizationStore;
import uk.gov.register.db.EntryDAO;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.db.ItemDAO;
import uk.gov.register.db.ItemQueryDAO;
import uk.gov.register.db.TransactionalEntryLog;
import uk.gov.register.db.TransactionalItemStore;
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
    public RegisterService(RegisterData registerData, DBI dbi, MemoizationStore memoizationStore, ItemValidator itemValidator, RegisterFieldsConfiguration registerFieldsConfiguration) {
        this.registerData = registerData;
        this.dbi = dbi;
        this.memoizationStore = memoizationStore;
        this.itemValidator = itemValidator;
        this.registerFieldsConfiguration = registerFieldsConfiguration;
    }

    public void asAtomicRegisterOperation(Consumer<Register> callback) {
        TransactionalMemoizationStore transactionalMemoizationStore = new TransactionalMemoizationStore(memoizationStore);
        PostgresDriverTransactional.useTransaction(dbi, postgresDriver -> {

            String registerName = registerData.getRegister().getRegisterName();
            Handle handle = postgresDriver.getHandle();
            TransactionalEntryLog entryLog = new TransactionalEntryLog(transactionalMemoizationStore, handle.attach(EntryQueryDAO.class), handle.attach(EntryDAO.class));
            TransactionalItemStore itemStore = new TransactionalItemStore(handle.attach(ItemDAO.class), handle.attach(ItemQueryDAO.class), itemValidator);
            Register register = new PostgresRegister(() -> registerName, postgresDriver, registerFieldsConfiguration, entryLog, itemStore);
            callback.accept(register);
            register.commit();
        });
        transactionalMemoizationStore.commitHashesToStore();
    }
}
