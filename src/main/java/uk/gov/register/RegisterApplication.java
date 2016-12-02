package uk.gov.register;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayBundle;
import io.dropwizard.flyway.FlywayFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.flywaydb.core.Flyway;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import uk.gov.organisation.client.GovukOrganisationClient;
import uk.gov.register.auth.AuthBundle;
import uk.gov.register.configuration.*;
import uk.gov.register.core.*;
import uk.gov.register.db.*;
import uk.gov.register.filters.CorsBundle;
import uk.gov.register.monitoring.CloudWatchHeartbeater;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.resources.SchemeContext;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.service.RegisterSerialisationFormatService;
import uk.gov.register.service.RegisterService;
import uk.gov.register.thymeleaf.ThymeleafViewRenderer;
import uk.gov.register.util.CanonicalJsonMapper;
import uk.gov.register.util.CanonicalJsonValidator;
import uk.gov.register.util.ObjectReconstructor;
import uk.gov.register.views.ViewFactory;
import uk.gov.verifiablelog.store.memoization.InMemoryPowOfTwoNoLeaves;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import javax.inject.Singleton;
import javax.ws.rs.client.Client;
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
        bootstrap.addBundle(new CorsBundle());

        bootstrap.addBundle(new FlywayBundle<RegisterConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(RegisterConfiguration configuration) {
                return configuration.getDatabase();
            }

            @Override
            public FlywayFactory getFlywayFactory(RegisterConfiguration configuration) {
                return configuration.getFlywayFactory();
            }
        });
        System.setProperty("java.protocol.handler.pkgs", "uk.gov.register.protocols");
    }

    @Override
    public void run(RegisterConfiguration configuration, Environment environment) throws Exception {
        DBIFactory dbiFactory = new DBIFactory();

        JerseyEnvironment jersey = environment.jersey();
        DropwizardResourceConfig resourceConfig = jersey.getResourceConfig();
        Client client = new JerseyClientBuilder(environment).using(configuration.getJerseyClientConfiguration()).build("http-client");
        Flyway flyway = configuration.getFlywayFactory().build(configuration.getDatabase().build(environment.metrics(), "flyway_db"));

        Optional<String> registersYamlFileUrl = Optional.ofNullable(System.getProperty("registersYaml"));
        Optional<String> fieldsYamlFileUrl = Optional.ofNullable(System.getProperty("fieldsYaml"));

        ConfigManager configManager = new ConfigManager(configuration, registersYamlFileUrl, fieldsYamlFileUrl);
        configManager.refreshConfig();

        RegistersConfiguration registersConfiguration = configManager.createRegistersConfiguration();
        FieldsConfiguration mintFieldsConfiguration = configManager.createFieldsConfiguration();

        AllTheRegisters allTheRegisters = configuration.getAllTheRegisters().build(dbiFactory, registersConfiguration, environment);
        jersey.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(flyway).to(Flyway.class);
                bind(mintFieldsConfiguration).to(FieldsConfiguration.class);
                bind(registersConfiguration).to(RegistersConfiguration.class);
                bindFactory(RegisterDataFactory.class).to(RegisterData.class);
                bindFactory(RegisterFieldsConfigurationFactory.class).to(RegisterFieldsConfiguration.class);
                bind(allTheRegisters);
                bindFactory(EverythingAboutARegisterProvider.class).to(EverythingAboutARegister.class);
                bindAsContract(RegisterFieldsConfiguration.class);
                bindFactory(Factories.EntryQueryDAOFactory.class).to(EntryQueryDAO.class);
                bindFactory(Factories.ItemQueryDAOFactory.class).to(ItemQueryDAO.class);
                bindFactory(Factories.RecordQueryDAOFactory.class).to(RecordQueryDAO.class);
                bind(new PublicBodiesConfiguration(Optional.ofNullable(System.getProperty("publicBodiesYaml")))).to(PublicBodiesConfiguration.class);

                bind(CanonicalJsonMapper.class).to(CanonicalJsonMapper.class);
                bind(CanonicalJsonValidator.class).to(CanonicalJsonValidator.class);
                bind(ItemValidator.class).to(ItemValidator.class);
                bind(ObjectReconstructor.class).to(ObjectReconstructor.class);
                bind(RegisterService.class).to(RegisterService.class);
                bind(RegisterSerialisationFormatService.class).to(RegisterSerialisationFormatService.class);

                bind(RequestContext.class).to(RequestContext.class).to(SchemeContext.class);
                bind(ViewFactory.class).to(ViewFactory.class).in(Singleton.class);
                bind(ItemConverter.class).to(ItemConverter.class).in(Singleton.class);
                bind(GovukOrganisationClient.class).to(GovukOrganisationClient.class).in(Singleton.class);
                bind(InMemoryPowOfTwoNoLeaves.class).to(MemoizationStore.class).in(Singleton.class);

                bind(PostgresRegister.class).to(Register.class).to(RegisterReadOnly.class);
                bind(UnmodifiableEntryLog.class).to(EntryLog.class);
                bind(UnmodifiableItemStore.class).to(ItemStore.class);
                bind(UnmodifiableRecordIndex.class).to(RecordIndex.class);
                bind(UriTemplateRegisterResolver.class).to(RegisterResolver.class);
                bind(configuration);
                bind(client).to(Client.class);
            }
        });

        resourceConfig.packages(
                "uk.gov.register.filters",
                "uk.gov.register.views.representations",
                "uk.gov.register.resources",
                "uk.gov.register.providers");

        if (configuration.cloudWatchEnvironmentName().isPresent()) {
            ScheduledExecutorService cloudwatch = environment.lifecycle().scheduledExecutorService("cloudwatch").threads(1).build();
            cloudwatch.scheduleAtFixedRate(new CloudWatchHeartbeater(configuration.cloudWatchEnvironmentName().get(), configuration.getRegisterName()), 0, 10000, TimeUnit.MILLISECONDS);
        }

        environment.getApplicationContext().setErrorHandler(new AssetsBundleCustomErrorHandler(environment));
    }
}


