package uk.gov.register;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.skife.jdbi.v2.DBI;
import uk.gov.organisation.client.GovukOrganisationClient;
import uk.gov.register.auth.AuthBundle;
import uk.gov.register.configuration.FieldsConfiguration;
import uk.gov.register.configuration.PublicBodiesConfiguration;
import uk.gov.register.configuration.RegistersConfiguration;
import uk.gov.register.core.PostgresRegister;
import uk.gov.register.core.Register;
import uk.gov.register.core.RegisterData;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.db.RegisterDAO;
import uk.gov.register.db.SchemaCreator;
import uk.gov.register.filters.UriDataFormatFilter;
import uk.gov.register.monitoring.CloudWatchHeartbeater;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.service.VerifiableLogService;
import uk.gov.register.thymeleaf.ThymeleafViewRenderer;
import uk.gov.register.util.ObjectReconstructor;
import uk.gov.register.views.ViewFactory;
import uk.gov.verifiablelog.store.memoization.InMemoryPowOfTwoNoLeaves;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import javax.inject.Singleton;
import javax.servlet.DispatcherType;
import javax.ws.rs.client.Client;
import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RegisterApplication extends Application<RegisterConfiguration> {
    public static void main(String[] args) {
        try {
            new RegisterApplication().run(args);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    @Override
    public String getName() {
        return "openregister";
    }

    @Override
    public void initialize(Bootstrap<RegisterConfiguration> bootstrap) {
        bootstrap.addBundle(new ViewBundle<>(ImmutableList.of(new ThymeleafViewRenderer("HTML5", "/templates/", ".html", false))));
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                ));
        bootstrap.addBundle(new AssetsBundle("/assets"));
        bootstrap.addBundle(new AuthBundle());
    }

    @Override
    public void run(RegisterConfiguration configuration, Environment environment) throws Exception {
        DBIFactory dbiFactory = new DBIFactory();
        DBI jdbi = dbiFactory.build(environment, configuration.getDatabase(), "postgres");

        EntryQueryDAO entryQueryDAO = jdbi.onDemand(EntryQueryDAO.class);

        RegisterDAO registerDAO = jdbi.onDemand(RegisterDAO.class);

        SchemaCreator schemaCreator = jdbi.onDemand(SchemaCreator.class);
        schemaCreator.ensureSchema();

        RegistersConfiguration registersConfiguration = new RegistersConfiguration(Optional.ofNullable(System.getProperty("registersYaml")));
        FieldsConfiguration mintFieldsConfiguration = new FieldsConfiguration(Optional.ofNullable(System.getProperty("fieldsYaml")));
        RegisterData registerData = registersConfiguration.getRegisterData(configuration.getRegister());

        JerseyEnvironment jersey = environment.jersey();
        DropwizardResourceConfig resourceConfig = jersey.getResourceConfig();

        Client client = new JerseyClientBuilder(environment).using(configuration.getJerseyClientConfiguration())
                .build("http-client");

        jersey.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(entryQueryDAO).to(EntryQueryDAO.class);
                bind(registerDAO).to(RegisterDAO.class);
                bind(mintFieldsConfiguration).to(FieldsConfiguration.class);
                bind(registersConfiguration).to(RegistersConfiguration.class);
                bind(registerData).to(RegisterData.class);
                bind(new PublicBodiesConfiguration(Optional.ofNullable(System.getProperty("publicBodiesYaml")))).to(PublicBodiesConfiguration.class);

                bind(ItemValidator.class).to(ItemValidator.class);
                bind(ObjectReconstructor.class).to(ObjectReconstructor.class);
                bind(VerifiableLogService.class).to(VerifiableLogService.class);

                bind(RequestContext.class).to(RequestContext.class);
                bind(ViewFactory.class).to(ViewFactory.class).in(Singleton.class);
                bind(ItemConverter.class).to(ItemConverter.class).in(Singleton.class);
                bind(GovukOrganisationClient.class).to(GovukOrganisationClient.class).in(Singleton.class);
                bind(InMemoryPowOfTwoNoLeaves.class).to(MemoizationStore.class).in(Singleton.class);

                bind(PostgresRegister.class).to(Register.class).to(RegisterReadOnly.class);
                bind(configuration);
                bind(client).to(Client.class);
            }
        });

        resourceConfig.packages(
                "uk.gov.register.filters",
                "uk.gov.register.views.representations",
                "uk.gov.register.resources",
                "uk.gov.register.providers");

        jersey.register(UriDataFormatFilter.class);

        if (configuration.cloudWatchEnvironmentName().isPresent()) {
            ScheduledExecutorService cloudwatch = environment.lifecycle().scheduledExecutorService("cloudwatch").threads(1).build();
            cloudwatch.scheduleAtFixedRate(new CloudWatchHeartbeater(configuration.cloudWatchEnvironmentName().get(), configuration.getRegister()), 0, 10000, TimeUnit.MILLISECONDS);
        }

        setCorsPreflight(environment.getApplicationContext());

        environment.getApplicationContext().setErrorHandler(new AssetsBundleCustomErrorHandler(environment));
    }

    private void setCorsPreflight(MutableServletContextHandler applicationContext) {
        FilterHolder filterHolder = applicationContext
                .addFilter(CrossOriginFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin");
        filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,HEAD");

        filterHolder.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "false");
    }
}


