package uk.gov.register.core;

import com.google.common.base.Throwables;
import org.flywaydb.core.Flyway;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.exceptions.CallbackFailedException;
import uk.gov.register.configuration.*;
import uk.gov.register.db.*;
import uk.gov.register.exceptions.NoSuchConfigException;
import uk.gov.register.service.ItemValidator;
import uk.gov.verifiablelog.store.memoization.InMemoryPowOfTwoNoLeaves;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class RegisterContext implements
        RegisterTrackingConfiguration,
        DeleteRegisterDataConfiguration,
        RegisterContentPagesConfiguration,
        ResourceConfiguration {
    private RegisterName registerName;
    private ConfigManager configManager;
    private AtomicReference<MemoizationStore> memoizationStore;
    private DBI dbi;
    private Flyway flyway;
    private final Optional<String> historyPageUrl;
    private final Optional<String> trackingId;
    private final boolean enableRegisterDataDelete;
    private final boolean enableDownloadResource;
    private RegisterMetadata registerMetadata;

    public RegisterContext(RegisterName registerName, ConfigManager configManager,
                           DBI dbi, Flyway flyway, Optional<String> trackingId, boolean enableRegisterDataDelete,
                           boolean enableDownloadResource, Optional<String> historyPageUrl) {
        this.registerName = registerName;
        this.configManager = configManager;
        this.dbi = dbi;
        this.flyway = flyway;
        this.historyPageUrl = historyPageUrl;
        this.memoizationStore = new AtomicReference<>(new InMemoryPowOfTwoNoLeaves());
        this.trackingId = trackingId;
        this.enableRegisterDataDelete = enableRegisterDataDelete;
        this.enableDownloadResource = enableDownloadResource;
        this.registerMetadata = configManager.getRegistersConfiguration().getRegisterMetadata(registerName);
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
                        handle.attach(CurrentKeysUpdateDAO.class)
                )
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

    public void resetRegister() throws IOException, NoSuchConfigException {
        if (enableRegisterDataDelete) {
            flyway.clean();
            configManager.refreshConfig();
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

    @Override
    public Optional<String> getRegisterHistoryPageUrl() {
        return historyPageUrl;
    }
}
