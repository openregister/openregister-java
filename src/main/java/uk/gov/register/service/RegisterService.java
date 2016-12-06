package uk.gov.register.service;

import com.google.common.base.Throwables;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.exceptions.CallbackFailedException;
import uk.gov.register.core.EverythingAboutARegister;
import uk.gov.register.core.Register;
import uk.gov.register.core.TransactionalMemoizationStore;

import javax.inject.Inject;
import java.util.function.Consumer;

public class RegisterService {
    private final EverythingAboutARegister aboutARegister;

    @Inject
    public RegisterService(EverythingAboutARegister aboutARegister) {
        this.aboutARegister = aboutARegister;
    }

    public void asAtomicRegisterOperation(Consumer<Register> callback) {
        TransactionalMemoizationStore transactionalMemoizationStore = new TransactionalMemoizationStore(aboutARegister.getMemoizationStore());
        useTransaction(aboutARegister.getDbi(), handle -> {
            Register register = aboutARegister.buildTransactionalRegister(handle, transactionalMemoizationStore);
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
