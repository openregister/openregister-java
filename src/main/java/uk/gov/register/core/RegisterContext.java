package uk.gov.register.core;

import org.apache.commons.codec.digest.DigestUtils;
import org.flywaydb.core.Flyway;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.exceptions.CallbackFailedException;
import uk.gov.register.auth.RegisterAuthenticator;
import uk.gov.register.configuration.ConfigManager;
import uk.gov.register.configuration.DeleteRegisterDataConfiguration;
import uk.gov.register.configuration.ResourceConfiguration;
import uk.gov.register.db.EntryDAO;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.db.ItemDAO;
import uk.gov.register.db.ItemQueryDAO;
import uk.gov.register.db.RecordQueryDAO;
import uk.gov.register.db.RecordSet;
import uk.gov.register.exceptions.FieldDefinitionException;
import uk.gov.register.exceptions.NoSuchConfigException;
import uk.gov.register.exceptions.RegisterDefinitionException;
import uk.gov.register.proofs.EntryMerkleLeafStore;
import uk.gov.register.proofs.V1EntryMerkleLeafStore;
import uk.gov.register.service.EnvironmentValidator;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.store.postgres.BatchedPostgresDataAccessLayer;
import uk.gov.register.store.postgres.PostgresDataAccessLayer;
import uk.gov.verifiablelog.VerifiableLog;
import uk.gov.verifiablelog.store.memoization.InMemoryPowOfTwoNoLeaves;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Throwables.throwIfUnchecked;


public class RegisterContext implements
        DeleteRegisterDataConfiguration,
        ResourceConfiguration {
    private RegisterId registerId;
    private ConfigManager configManager;
    private final EnvironmentValidator environmentValidator;
    private AtomicReference<MemoizationStore> memoizationStoreV1;
    private AtomicReference<MemoizationStore> memoizationStoreV2;
    private DBI dbi;
    private Flyway flyway;
    private final String schema;
    private final boolean enableRegisterDataDelete;
    private final boolean enableDownloadResource;
    private RegisterAuthenticator authenticator;
    private final ItemValidator itemValidator;
    private boolean hasConsistentState;

    public RegisterContext(RegisterId registerId, ConfigManager configManager, EnvironmentValidator environmentValidator,
                           DBI dbi, Flyway flyway, String schema, boolean enableRegisterDataDelete,
                           boolean enableDownloadResource, RegisterAuthenticator authenticator) {
        this.registerId = registerId;
        this.configManager = configManager;
        this.environmentValidator = environmentValidator;
        this.dbi = dbi;
        this.flyway = flyway;
        this.schema = schema;
        this.memoizationStoreV1 = new AtomicReference<>(new InMemoryPowOfTwoNoLeaves());
        this.memoizationStoreV2 = new AtomicReference<>(new InMemoryPowOfTwoNoLeaves());
        this.enableRegisterDataDelete = enableRegisterDataDelete;
        this.enableDownloadResource = enableDownloadResource;
        this.authenticator = authenticator;
        this.itemValidator = new ItemValidator(registerId);
        this.hasConsistentState = true;
    }

    public RegisterId getRegisterId() {
        return registerId;
    }

    public RegisterMetadata getRegisterMetadata() {
        return configManager.getRegistersConfiguration().getRegisterMetadata(registerId);
    }

    public int migrate() {
        flyway.setSchemas(schema);

        return flyway.migrate();
    }

    public EntryLog buildEntryLog() {
        DataAccessLayer dataAccessLayer = getOnDemandDataAccessLayer();

        return new EntryLogImpl(dataAccessLayer);
    }

    public Register buildOnDemandRegister() {
        DataAccessLayer dataAccessLayer = getOnDemandDataAccessLayer();

        return new RegisterImpl(registerId,
                new EntryLogImpl(dataAccessLayer),
                new ItemStoreImpl(dataAccessLayer),
                new RecordSet(dataAccessLayer),
                itemValidator,
                environmentValidator);
    }

    private Register buildTransactionalRegister(DataAccessLayer dataAccessLayer, TransactionalMemoizationStore memoizationStore) {
        return new RegisterImpl(registerId,
                new EntryLogImpl(dataAccessLayer),
                new ItemStoreImpl(dataAccessLayer),
                new RecordSet(dataAccessLayer),
                itemValidator,
                environmentValidator);
    }

    public void transactionalRegisterOperation(Consumer<Register> consumer) {
        TransactionalMemoizationStore transactionalMemoizationStore = new TransactionalMemoizationStore(memoizationStoreV1.get());
        useTransaction(dbi, handle -> {
            BatchedPostgresDataAccessLayer dataAccessLayer = getTransactionalDataAccessLayer(handle);
            Register register = buildTransactionalRegister(dataAccessLayer, transactionalMemoizationStore);
            consumer.accept(register);
            dataAccessLayer.writeBatchesToDatabase();
        });
        transactionalMemoizationStore.commitHashesToStore();
    }

    public void resetRegister() throws IOException, NoSuchConfigException {
        if (enableRegisterDataDelete) {
            flyway.clean();
            configManager.refreshConfig();
            memoizationStoreV1.set(new InMemoryPowOfTwoNoLeaves());
            memoizationStoreV2.set(new InMemoryPowOfTwoNoLeaves());
            flyway.migrate();

            hasConsistentState = true;
        }
    }

    public static void useTransaction(DBI dbi, Consumer<Handle> callback) {
        try {
            dbi.useTransaction(TransactionIsolationLevel.SERIALIZABLE, (handle, status) -> callback.accept(handle));
        } catch (CallbackFailedException e) {
            throwIfUnchecked(e);
            throw new RuntimeException(e.getCause());
        }
    }

    private DataAccessLayer getOnDemandDataAccessLayer() {
        return new PostgresDataAccessLayer(
                dbi.onDemand(EntryDAO.class),
                dbi.onDemand(EntryQueryDAO.class),
                dbi.onDemand(ItemDAO.class),
                dbi.onDemand(ItemQueryDAO.class),
                dbi.onDemand(RecordQueryDAO.class),
                schema);
    }

    public <R> R withV1VerifiableLog(Function<VerifiableLog, R> callback) {
        return getOnDemandDataAccessLayer().withEntryIterator(entryIterator -> {
            VerifiableLog verifiableLog = new VerifiableLog(
                    DigestUtils.getSha256Digest(),
                    new V1EntryMerkleLeafStore(entryIterator),
                    memoizationStoreV1.get());
            return callback.apply(verifiableLog);
        });
    }

    public <R> R withVerifiableLog(Function<VerifiableLog, R> callback) {
        return getOnDemandDataAccessLayer().withEntryIterator(entryIterator -> {
            VerifiableLog verifiableLog = new VerifiableLog(
                    DigestUtils.getSha256Digest(),
                    new EntryMerkleLeafStore(entryIterator),
                    memoizationStoreV2.get());
            return callback.apply(verifiableLog);
        });
    }

    private BatchedPostgresDataAccessLayer getTransactionalDataAccessLayer(Handle handle) {
        return new BatchedPostgresDataAccessLayer(
                new PostgresDataAccessLayer(
                        handle.attach(EntryDAO.class),
                        handle.attach(EntryQueryDAO.class),
                        handle.attach(ItemDAO.class),
                        handle.attach(ItemQueryDAO.class),
                        handle.attach(RecordQueryDAO.class),
                        schema));
    }

    public void validate() {
        try {
            environmentValidator.validateExistingMetadataAgainstEnvironment(this);
        } catch (FieldDefinitionException | RegisterDefinitionException ex) {
            hasConsistentState = false;
        }
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

    public String getSchema() {
        return schema;
    }

    public boolean hasConsistentState() {
        return hasConsistentState;
    }
}
