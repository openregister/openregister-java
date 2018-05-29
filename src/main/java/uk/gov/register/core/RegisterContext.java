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
import uk.gov.register.db.Index;
import uk.gov.register.exceptions.FieldDefinitionException;
import uk.gov.register.exceptions.NoSuchConfigException;
import uk.gov.register.exceptions.RegisterDefinitionException;
import uk.gov.register.indexer.IndexDriver;
import uk.gov.register.indexer.function.IndexFunction;
import uk.gov.register.service.EnvironmentValidator;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.store.postgres.PostgresDataAccessLayer;
import uk.gov.verifiablelog.store.memoization.InMemoryPowOfTwoNoLeaves;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

public class RegisterContext implements
        DeleteRegisterDataConfiguration,
        HomepageContentConfiguration,
        IndexConfiguration,
        ResourceConfiguration {
    private RegisterId registerId;
    private ConfigManager configManager;
    private final EnvironmentValidator environmentValidator;
    private AtomicReference<MemoizationStore> memoizationStore;
    private DBI dbi;
    private Flyway flyway;
    private final String schema;
    private final List<IndexFunctionConfiguration> indexFunctionConfigs;
    private final boolean enableRegisterDataDelete;
    private final boolean enableDownloadResource;
    private RegisterAuthenticator authenticator;
    private final ItemValidator itemValidator;
    private boolean hasConsistentState;

    public RegisterContext(RegisterId registerId, ConfigManager configManager, EnvironmentValidator environmentValidator,
                           DBI dbi, Flyway flyway, String schema,
                           boolean enableRegisterDataDelete, boolean enableDownloadResource,
                           List<String> indexNames, RegisterAuthenticator authenticator) {
        this.registerId = registerId;
        this.configManager = configManager;
        this.environmentValidator = environmentValidator;
        this.dbi = dbi;
        this.flyway = flyway;
        this.schema = schema;
        this.indexFunctionConfigs = mapIndexes(indexNames);
        this.memoizationStore = new AtomicReference<>(new InMemoryPowOfTwoNoLeaves());
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

    private Map<EntryType, Collection<IndexFunction>> getIndexFunctions() {
        Map<EntryType, Collection<IndexFunction>> indexFunctionsByEntryType = new HashMap<>();
        indexFunctionsByEntryType.put(EntryType.user, new ArrayList<>());
        indexFunctionsByEntryType.put(EntryType.system, new ArrayList<>());

        for (IndexFunctionConfiguration indexFunctionConfig : indexFunctionConfigs) {
            indexFunctionsByEntryType.get(indexFunctionConfig.getEntryType()).addAll(indexFunctionConfig.getIndexFunctions());
        }
        return indexFunctionsByEntryType;
    }

    public Register buildOnDemandRegister() {
        DataAccessLayer dataAccessLayer = getOnDemandDataAccessLayer();

        return new PostgresRegister(registerId,
                new EntryLogImpl(dataAccessLayer, memoizationStore.get()),
                new ItemStoreImpl(dataAccessLayer),
                new Index(dataAccessLayer),
                getIndexFunctions(),
                itemValidator,
                environmentValidator);
    }

    private Register buildTransactionalRegister(DataAccessLayer dataAccessLayer, TransactionalMemoizationStore memoizationStore) {
        return new PostgresRegister(registerId,
                new EntryLogImpl(dataAccessLayer, memoizationStore),
                new ItemStoreImpl(dataAccessLayer),
                new Index(dataAccessLayer),
                getIndexFunctions(),
                itemValidator,
                environmentValidator);
    }

    public void transactionalRegisterOperation(Consumer<Register> consumer) {
        TransactionalMemoizationStore transactionalMemoizationStore = new TransactionalMemoizationStore(memoizationStore.get());
        useTransaction(dbi, handle -> {
            PostgresDataAccessLayer dataAccessLayer = getTransactionalDataAccessLayer(handle);
            Register register = buildTransactionalRegister(dataAccessLayer, transactionalMemoizationStore);
            consumer.accept(register);
            dataAccessLayer.checkpoint();
        });
        transactionalMemoizationStore.commitHashesToStore();
    }

    public void resetRegister() throws IOException, NoSuchConfigException {
        if (enableRegisterDataDelete) {
            flyway.clean();
            configManager.refreshConfig();
            memoizationStore.set(new InMemoryPowOfTwoNoLeaves());
            flyway.migrate();

            hasConsistentState = true;
        }
    }

    public static void useTransaction(DBI dbi, Consumer<Handle> callback) {
        try {
            dbi.useTransaction(TransactionIsolationLevel.SERIALIZABLE, (handle, status) -> callback.accept(handle));
        } catch (CallbackFailedException e) {
            throw Throwables.propagate(e.getCause());
        }
    }

    private List<IndexFunctionConfiguration> mapIndexes(List<String> indexNames) {
        return IndexFunctionConfiguration.getConfigurations(indexNames);
    }

    private DataAccessLayer getOnDemandDataAccessLayer() {
        return new PostgresDataAccessLayer(
                dbi.onDemand(EntryQueryDAO.class),
                dbi.onDemand(IndexDAO.class),
                dbi.onDemand(IndexQueryDAO.class),
                dbi.onDemand(EntryDAO.class),
                dbi.onDemand(EntryItemDAO.class),
                dbi.onDemand(ItemQueryDAO.class),
                dbi.onDemand(ItemDAO.class),
                schema,
                new IndexDriver(),
                getIndexFunctions());
    }

    private PostgresDataAccessLayer getTransactionalDataAccessLayer(Handle handle) {
        return new PostgresDataAccessLayer(
                handle.attach(EntryQueryDAO.class),
                handle.attach(IndexDAO.class),
                handle.attach(IndexQueryDAO.class),
                handle.attach(EntryDAO.class),
                handle.attach(EntryItemDAO.class),
                handle.attach(ItemQueryDAO.class),
                handle.attach(ItemDAO.class),
                schema,
                new IndexDriver(),
                getIndexFunctions());
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

    @Override
    public List<String> getIndexes() {
        return indexFunctionConfigs.stream().map(IndexFunctionConfiguration::getName).collect(toList());
    }

    public String getSchema() {
        return schema;
    }

    public boolean hasConsistentState() {
        return hasConsistentState;
    }
}
