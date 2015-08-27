package uk.gov;

import com.google.common.base.Throwables;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;
import uk.gov.mint.LoadHandler;
import uk.gov.mint.MintService;
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
        DBI jdbi = dbiFactory.build(environment, configuration.getDatabase(), "postgres");

        EntriesUpdateDAO entriesUpdateDAO = jdbi.onDemand(EntriesUpdateDAO.class);


        LoadHandler loadHandler = new LoadHandler(entriesUpdateDAO);

        JerseyEnvironment jersey = environment.jersey();
        jersey.register(new MintService(loadHandler));
    }
}


