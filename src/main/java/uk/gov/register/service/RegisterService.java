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

import javax.inject.Inject;
import java.util.function.Consumer;

public class RegisterService {
    private final ItemValidator itemValidator;
    private final EverythingAboutARegister aboutARegister;

    @Inject
    public RegisterService(ItemValidator itemValidator, EverythingAboutARegister aboutARegister) {
        this.itemValidator = itemValidator;
        this.aboutARegister = aboutARegister;
    }

    public void asAtomicRegisterOperation(Consumer<Register> callback) {
        TransactionalMemoizationStore transactionalMemoizationStore = new TransactionalMemoizationStore(aboutARegister.getMemoizationStore());
        useTransaction(aboutARegister.getDbi(), handle -> {

            EntryLog entryLog = new TransactionalEntryLog(transactionalMemoizationStore, handle.attach(EntryQueryDAO.class), handle.attach(EntryDAO.class));
            ItemStore itemStore = new TransactionalItemStore(handle.attach(ItemDAO.class), handle.attach(ItemQueryDAO.class), itemValidator);
            RecordIndex recordIndex = new TransactionalRecordIndex(handle.attach(RecordQueryDAO.class), handle.attach(CurrentKeysUpdateDAO.class));
            Register register = new PostgresRegister(aboutARegister.getRegisterData(), aboutARegister.getRegisterFieldsConfiguration(), entryLog, itemStore, recordIndex);
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
