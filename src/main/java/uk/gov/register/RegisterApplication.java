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
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import uk.gov.organisation.client.GovukOrganisationClient;
import uk.gov.register.auth.AuthBundle;
import uk.gov.register.configuration.FieldsConfiguration;
import uk.gov.register.configuration.PublicBodiesConfiguration;
import uk.gov.register.configuration.RegisterFieldsConfiguration;
import uk.gov.register.configuration.RegistersConfiguration;
import uk.gov.register.core.AllTheRegisters;
import uk.gov.register.core.EverythingAboutARegister;
import uk.gov.register.core.EverythingAboutARegisterProvider;
import uk.gov.register.core.PostgresRegister;
import uk.gov.register.core.Register;
import uk.gov.register.core.RegisterData;
import uk.gov.register.core.RegisterDataFactory;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.core.UriTemplateRegisterResolver;
import uk.gov.register.filters.CorsBundle;
import uk.gov.register.monitoring.CloudWatchHeartbeater;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.resources.SchemeContext;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.service.RegisterSerialisationFormatService;
import uk.gov.register.service.RegisterService;
import uk.gov.register.store.BackingStoreDriver;
import uk.gov.register.store.postgres.PostgresDriverNonTransactional;
import uk.gov.register.thymeleaf.ThymeleafViewRenderer;
import uk.gov.register.util.CanonicalJsonMapper;
import uk.gov.register.util.CanonicalJsonValidator;
import uk.gov.register.util.ObjectReconstructor;
import uk.gov.register.views.ViewFactory;

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
    }

    @Override
    public void run(RegisterConfiguration configuration, Environment environment) throws Exception {
        DBIFactory dbiFactory = new DBIFactory();

        RegistersConfiguration registersConfiguration = new RegistersConfiguration(Optional.ofNullable(System.getProperty("registersYaml")));
        FieldsConfiguration mintFieldsConfiguration = new FieldsConfiguration(Optional.ofNullable(System.getProperty("fieldsYaml")));

        JerseyEnvironment jersey = environment.jersey();
        DropwizardResourceConfig resourceConfig = jersey.getResourceConfig();

        Client client = new JerseyClientBuilder(environment).using(configuration.getJerseyClientConfiguration()).build("http-client");

        AllTheRegisters allTheRegisters = configuration.getAllTheRegisters().build(dbiFactory, registersConfiguration, environment);

        allTheRegisters.stream().forEach(register -> register.getFlyway().migrate());

        jersey.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(mintFieldsConfiguration).to(FieldsConfiguration.class);
                bind(registersConfiguration).to(RegistersConfiguration.class);
                bindFactory(RegisterDataFactory.class).to(RegisterData.class);
                bind(allTheRegisters);
                bindFactory(EverythingAboutARegisterProvider.class).to(EverythingAboutARegister.class);
                bindAsContract(RegisterFieldsConfiguration.class);
                bind(new PublicBodiesConfiguration(Optional.ofNullable(System.getProperty("publicBodiesYaml")))).to(PublicBodiesConfiguration.class);

                bind(CanonicalJsonMapper.class).to(CanonicalJsonMapper.class);
                bind(CanonicalJsonValidator.class).to(CanonicalJsonValidator.class);
                bind(ItemValidator.class).to(ItemValidator.class);
                bind(ObjectReconstructor.class).to(ObjectReconstructor.class);
                bind(PostgresDriverNonTransactional.class).to(BackingStoreDriver.class);
                bind(RegisterService.class).to(RegisterService.class);
                bind(RegisterSerialisationFormatService.class).to(RegisterSerialisationFormatService.class);

                bind(RequestContext.class).to(RequestContext.class).to(SchemeContext.class);
                bind(ViewFactory.class).to(ViewFactory.class).in(Singleton.class);
                bind(ItemConverter.class).to(ItemConverter.class).in(Singleton.class);
                bind(GovukOrganisationClient.class).to(GovukOrganisationClient.class).in(Singleton.class);

                bind(PostgresRegister.class).to(Register.class).to(RegisterReadOnly.class);
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
            cloudwatch.scheduleAtFixedRate(new CloudWatchHeartbeater(configuration.cloudWatchEnvironmentName().get(), configuration.getDefaultRegisterName()), 0, 10000, TimeUnit.MILLISECONDS);
        }

        environment.getApplicationContext().setErrorHandler(new AssetsBundleCustomErrorHandler(environment));
    }
}


