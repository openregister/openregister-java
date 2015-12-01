package uk.gov;

import com.google.common.base.Throwables;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;
import uk.gov.mint.EntryValidator;
import uk.gov.mint.LoadHandler;
import uk.gov.mint.MintService;
import uk.gov.register.FieldsConfiguration;
import uk.gov.register.RegistersConfiguration;
import uk.gov.store.EntriesUpdateDAO;

public class MintApplication extends Application<MintConfiguration> {

    public static void main(String[] args) {
        try {
            new MintApplication().run(args);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    @Override
    public String getName() {
        return "mint";
    }

    @Override
    public void initialize(Bootstrap<MintConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                ));
    }

    @Override
    public void run(MintConfiguration configuration, Environment environment) throws Exception {
        DBIFactory dbiFactory = new DBIFactory();

        DataSourceFactory database = configuration.getDatabase();

        DBI jdbi = dbiFactory.build(environment, database, "postgres");

        EntriesUpdateDAO entriesUpdateDAO = jdbi.onDemand(EntriesUpdateDAO.class);

        RegistersConfiguration registersConfiguration = new RegistersConfiguration();

        FieldsConfiguration fieldsConfiguration = new FieldsConfiguration();

        EntryValidator entryValidator = new EntryValidator(registersConfiguration, fieldsConfiguration);

        LoadHandler loadHandler = new LoadHandler(entriesUpdateDAO, entryValidator);

        JerseyEnvironment jersey = environment.jersey();
        jersey.register(new MintService(loadHandler));
    }
}


