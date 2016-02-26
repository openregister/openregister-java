package uk.gov;

import com.google.common.base.Throwables;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;
import uk.gov.mint.*;
import uk.gov.mint.monitoring.CloudWatchHeartbeater;
import uk.gov.register.FieldsConfiguration;
import uk.gov.register.RegistersConfiguration;
import uk.gov.store.EntriesUpdateDAO;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

        RegistersConfiguration registersConfiguration = new RegistersConfiguration(Optional.ofNullable(System.getProperty("registersYaml")));
        FieldsConfiguration fieldsConfiguration = new FieldsConfiguration(Optional.ofNullable(System.getProperty("fieldsYaml")));

        EntryValidator entryValidator = new EntryValidator(registersConfiguration, fieldsConfiguration);
        ObjectReconstructor objectReconstructor = new ObjectReconstructor();

        Loader handler;
        if (configuration.getCTServer().isPresent()) {
            handler = new CTHandler(configuration, environment, getName());
        } else {
            handler = new LoadHandler(jdbi.onDemand(EntriesUpdateDAO.class));
        }

        JerseyEnvironment jersey = environment.jersey();
        jersey.register(new MintService(configuration.getRegister(), objectReconstructor, entryValidator, handler));

        jersey.register(CTExceptionMapper.class);
        jersey.register(EntryValidationExceptionMapper.class);
        jersey.register(JsonParseExceptionMapper.class);
        jersey.register(ThrowableExceptionMapper.class);


        configuration.getAuthenticator().build()
                .ifPresent(authenticator ->
                        jersey.register(new AuthDynamicFeature(
                                new BasicCredentialAuthFilter.Builder<User>()
                                        .setAuthenticator(authenticator)
                                        .buildAuthFilter()
                        ))
                );

        if (configuration.cloudWatchEnvironmentName().isPresent()) {
            ScheduledExecutorService cloudwatch = environment.lifecycle().scheduledExecutorService("cloudwatch").threads(1).build();
            cloudwatch.scheduleAtFixedRate(new CloudWatchHeartbeater(configuration.cloudWatchEnvironmentName().get(), configuration.getRegister()), 0, 10000, TimeUnit.MILLISECONDS);
        }
    }
}


