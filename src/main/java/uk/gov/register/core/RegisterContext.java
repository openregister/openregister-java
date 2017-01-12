package uk.gov.register.core;

import com.google.common.base.Throwables;
import org.flywaydb.core.Flyway;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.exceptions.CallbackFailedException;
import uk.gov.register.auth.RegisterAuthenticator;
import uk.gov.register.configuration.*;
import uk.gov.register.db.*;
import uk.gov.register.exceptions.NoSuchConfigException;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.service.RegisterLinkService;
import uk.gov.verifiablelog.store.memoization.InMemoryPowOfTwoNoLeaves;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class RegisterContext implements
        RegisterTrackingConfiguration,
        DeleteRegisterDataConfiguration,
        HomepageContentConfiguration,
        ResourceConfiguration {
    private RegisterName registerName;
    private ConfigManager configManager;
    private RegisterLinkService registerLinkService;
    private AtomicReference<MemoizationStore> memoizationStore;
    private DBI dbi;
    private Flyway flyway;
    private final Optional<String> historyPageUrl;
    private final Optional<String> custodianName;
    private final Optional<String> trackingId;
    private final Optional<List<String>> similarRegisters;
    private final boolean enableRegisterDataDelete;
    private final boolean enableDownloadResource;
    private RegisterMetadata registerMetadata;
    private RegisterAuthenticator authenticator;

    public RegisterContext(RegisterName registerName, ConfigManager configManager, RegisterLinkService registerLinkService,
                           DBI dbi, Flyway flyway, Optional<String> trackingId, boolean enableRegisterDataDelete,
                           boolean enableDownloadResource, Optional<String> historyPageUrl,
                           Optional<String> custodianName, Optional<List<String>> similarRegisters,
                           RegisterAuthenticator authenticator) {
        this.registerName = registerName;
        this.configManager = configManager;
        this.registerLinkService = registerLinkService;
        this.dbi = dbi;
        this.flyway = flyway;
        this.historyPageUrl = historyPageUrl;
        this.custodianName = custodianName;
        this.similarRegisters = similarRegisters;
        this.memoizationStore = new AtomicReference<>(new InMemoryPowOfTwoNoLeaves());
        this.trackingId = trackingId;
        this.enableRegisterDataDelete = enableRegisterDataDelete;
        this.enableDownloadResource = enableDownloadResource;
        this.registerMetadata = configManager.getRegistersConfiguration().getRegisterMetadata(registerName);
        this.authenticator = authenticator;
    }

    public RegisterName getRegisterName() {
        return registerName;
    }

    private RegisterFieldsConfiguration getRegisterFieldsConfiguration() {
        return new RegisterFieldsConfiguration(getRegisterMetadata().getFields());
    }

    public RegisterMetadata getRegisterMetadata() {
        return registerMetadata;
    }

    public Flyway getFlyway() {
        return flyway;
    }

    public Register buildOnDemandRegister() {
        return new PostgresRegister(getRegisterMetadata(),
                getRegisterFieldsConfiguration(),
                new UnmodifiableEntryLog(memoizationStore.get(), dbi.onDemand(EntryQueryDAO.class)),
                new UnmodifiableItemStore(dbi.onDemand(ItemQueryDAO.class)),
                new UnmodifiableRecordIndex(dbi.onDemand(RecordQueryDAO.class))
        );
    }

    private Register buildTransactionalRegister(Handle handle, TransactionalMemoizationStore memoizationStore) {
        return new PostgresRegister(getRegisterMetadata(),
                getRegisterFieldsConfiguration(),
                new TransactionalEntryLog(memoizationStore,
                        handle.attach(EntryQueryDAO.class),
                        handle.attach(EntryDAO.class)),
                new TransactionalItemStore(
                        handle.attach(ItemDAO.class),
                        handle.attach(ItemQueryDAO.class),
                        new ItemValidator(configManager, registerName)),
                new TransactionalRecordIndex(
                        handle.attach(RecordQueryDAO.class),
                        handle.attach(CurrentKeysUpdateDAO.class))
        );
    }

    public void transactionalRegisterOperation(Consumer<Register> consumer) {
        TransactionalMemoizationStore transactionalMemoizationStore = new TransactionalMemoizationStore(memoizationStore.get());
        useTransaction(dbi, handle -> {
            Register register = buildTransactionalRegister(handle, transactionalMemoizationStore);
            consumer.accept(register);
            register.commit();
        });
        transactionalMemoizationStore.commitHashesToStore();
    }

    public void transactionalRegisterOperation(Function<Register, Boolean> registerOperationFunc) {
        TransactionalMemoizationStore transactionalMemoizationStore = new TransactionalMemoizationStore(memoizationStore.get());

        Handle handle = null;
        try {
            handle = dbi.open();
            handle.begin();

            Register register = buildTransactionalRegister(handle, transactionalMemoizationStore);
            Boolean shouldCommit = registerOperationFunc.apply(register);

            if (shouldCommit) {
                register.commit();
                transactionalMemoizationStore.commitHashesToStore();
                handle.commit();
            } else {
                transactionalMemoizationStore.rollbackHashesFromStore();
                handle.rollback();
            }

        } catch (Exception e) {
            if (handle != null) {
                handle.rollback();
            }
            throw new RuntimeException(e);
        } finally {
            if (handle != null && !handle.isClosed()) {
                handle.close();
            }
        }
    }


    public void resetRegister() throws IOException, NoSuchConfigException {
        if (enableRegisterDataDelete) {
            flyway.clean();
            configManager.refreshConfig();
            registerLinkService.updateRegisterLinks(registerName);
            memoizationStore.set(new InMemoryPowOfTwoNoLeaves());
            flyway.migrate();
        }
    }

    public static void useTransaction(DBI dbi, Consumer<Handle> callback) {
        try {
            dbi.useTransaction(TransactionIsolationLevel.SERIALIZABLE, (handle, status) -> callback.accept(handle));
        } catch (CallbackFailedException e) {
            throw Throwables.propagate(e.getCause());
        }
    }

    @Override
    public Optional<String> getRegisterTrackingId() {
        return trackingId;
    }

    @Override
    public boolean getEnableRegisterDataDelete() {
        return enableRegisterDataDelete;
    }

    @Override
    public boolean getEnableDownloadResource() {
        return enableDownloadResource;
    }

    public RegisterAuthenticator getAuthenticator() {
        return authenticator;
    }

    @Override
    public Optional<String> getRegisterHistoryPageUrl() {
        return historyPageUrl;
    }

    @Override
    public Optional<String> getCustodianName() {
        return custodianName;
    }

    @Override
    public Optional<List<String>> getSimilarRegisters() {
        return similarRegisters;
    }
}
