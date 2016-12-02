package uk.gov.register.service;

import com.google.common.base.Throwables;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.exceptions.CallbackFailedException;
import uk.gov.register.configuration.RegisterFieldsConfiguration;
import uk.gov.register.core.EntryLog;
import uk.gov.register.core.EverythingAboutARegister;
import uk.gov.register.core.ItemStore;
import uk.gov.register.core.PostgresRegister;
import uk.gov.register.core.RecordIndex;
import uk.gov.register.core.Register;
import uk.gov.register.core.RegisterData;
import uk.gov.register.core.TransactionalMemoizationStore;
import uk.gov.register.db.CurrentKeysUpdateDAO;
import uk.gov.register.db.EntryDAO;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.db.ItemDAO;
import uk.gov.register.db.ItemQueryDAO;
import uk.gov.register.db.RecordQueryDAO;
import uk.gov.register.db.TransactionalEntryLog;
import uk.gov.register.db.TransactionalItemStore;
import uk.gov.register.db.TransactionalRecordIndex;
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
        TransactionalMemoizationStore transactionalMemoizationStore = new TransactionalMemoizationStore(memoizationStore);
        useTransaction(dbi, handle -> {

            EntryLog entryLog = new TransactionalEntryLog(transactionalMemoizationStore, handle.attach(EntryQueryDAO.class), handle.attach(EntryDAO.class));
            ItemStore itemStore = new TransactionalItemStore(handle.attach(ItemDAO.class), handle.attach(ItemQueryDAO.class), itemValidator);
            RecordIndex recordIndex = new TransactionalRecordIndex(handle.attach(RecordQueryDAO.class), handle.attach(CurrentKeysUpdateDAO.class));
            Register register = new PostgresRegister(registerData, registerFieldsConfiguration, entryLog, itemStore, recordIndex);
            callback.accept(register);
            register.commit();
        });
        transactionalMemoizationStore.commitHashesToStore();
    }

    public static void useTransaction(DBI dbi, Consumer<Handle> callback) {
        try {
            dbi.useTransaction(TransactionIsolationLevel.SERIALIZABLE, (handle, status) ->
                    callback.accept(handle)
            );
        } catch (CallbackFailedException e) {
            throw Throwables.propagate(e.getCause());
        }
    }
}
