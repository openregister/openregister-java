package uk.gov.register.commands;

import io.dropwizard.Application;
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import uk.gov.register.RegisterConfiguration;
import uk.gov.register.configuration.ConfigManager;
import uk.gov.register.configuration.DatabaseManager;
import uk.gov.register.core.AllTheRegisters;
import uk.gov.register.service.EnvironmentValidator;

public class MigrateCommand extends EnvironmentCommand<RegisterConfiguration> {

    private static boolean isRunningOnCloudFoundry() {
        return System.getenv().containsKey("CF_INSTANCE_GUID");
    }

    public MigrateCommand(Application<RegisterConfiguration> application) {
        super(application, "migrate", "Run database migrations for all the registers");
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);
    }

    @Override
    public void run(Environment environment, Namespace namespace, RegisterConfiguration configuration) throws Exception {
        ConfigManager configManager = new ConfigManager(configuration);
        configManager.refreshConfig();

        EnvironmentValidator environmentValidator = new EnvironmentValidator(configManager);

        DBIFactory dbiFactory = new DBIFactory();
        DatabaseManager databaseManager = new DatabaseManager(configuration, environment, dbiFactory, isRunningOnCloudFoundry());

        AllTheRegisters allTheRegisters = configuration.getAllTheRegisters().build(configManager, databaseManager, environmentValidator, configuration);
        allTheRegisters.stream().forEach(registerContext -> {
            registerContext.migrate();
            registerContext.validate();

            if(!registerContext.hasConsistentState()) {
                throw new RuntimeException(String.format("WARNING: Register '%s' doesn't match its specification! API requests will fail! Aborting further migrations.", registerContext.getRegisterId().value()));
            }
        });
    }
}
