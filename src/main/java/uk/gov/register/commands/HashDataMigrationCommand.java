package uk.gov.register.commands;

import io.dropwizard.Application;
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import org.skife.jdbi.v2.DBI;
import uk.gov.register.RegisterConfiguration;
import uk.gov.register.configuration.ConfigManager;
import uk.gov.register.configuration.DatabaseManager;
import uk.gov.register.core.AllTheRegisters;
import uk.gov.register.core.Register;
import uk.gov.register.core.RegisterContext;
import uk.gov.register.service.EnvironmentValidator;
import uk.gov.register.util.JsonToBlobHash;

// TODO: remove once hashes on production are correctly populated
public class HashDataMigrationCommand extends EnvironmentCommand<RegisterConfiguration> {
    public HashDataMigrationCommand(Application<RegisterConfiguration> application) {
        super(application, "migrateHashes", "Populate hashes for V2 hashing algorithm");
    }

    private static boolean isRunningOnCloudFoundry() {
        return System.getenv().containsKey("CF_INSTANCE_GUID");
    }

    @Override
    protected void run(Environment environment, Namespace namespace, RegisterConfiguration configuration) throws Exception {
        ConfigManager configManager = new ConfigManager(configuration);
        configManager.refreshConfig();
        EnvironmentValidator environmentValidator = new EnvironmentValidator(configManager);
        DBIFactory dbiFactory = new DBIFactory();
        DatabaseManager databaseManager = new DatabaseManager(configuration, environment, dbiFactory, isRunningOnCloudFoundry());
        DBI dbi = databaseManager.getDbi();

        AllTheRegisters allTheRegisters = configuration.getAllTheRegisters().build(configManager, databaseManager, environmentValidator, configuration);

        allTheRegisters.stream().forEach(registerContext -> {
            String registerName = registerContext.getRegisterId().value();
            String schema = registerContext.getSchema();
            Register register = registerContext.buildOnDemandRegister();

            migrateRegister(dbi, schema, register);

            if(!registerContext.hasConsistentState()) {
                throw new RuntimeException(String.format("WARNING: Register '%s' doesn't match its specification! API requests will fail! Aborting further migrations.", registerName));
            }
        });
    }

    private void migrateRegister(DBI dbi, String schema, Register register) {
        RegisterContext.useTransaction(dbi, handle -> {
            register.getAllItems().stream().forEach(item -> {
                String oldHashValue = item.getSha256hex().getValue();
                String newHashValue = JsonToBlobHash.apply(item.getContent()).getValue();
                System.out.println("Updating " + oldHashValue + " -> " + newHashValue);

                handle.execute("update " + schema + ".item set blob_hash=? where sha256hex = ?", newHashValue, oldHashValue);
                handle.execute("update " + schema + ".entry set blob_hash=? where sha256hex = ?", newHashValue, oldHashValue);
                handle.execute("update " + schema + ".entry_system set blob_hash=? where sha256hex = ?", newHashValue, oldHashValue);
            });
        });
    }
}
