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
import uk.gov.register.configuration.*;
import uk.gov.register.core.*;
import uk.gov.register.db.*;
import uk.gov.register.filters.CorsBundle;
import uk.gov.register.monitoring.CloudWatchHeartbeater;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.resources.SchemeContext;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.service.RegisterSerialisationFormatService;
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

        System.setProperty("java.protocol.handler.pkgs", "uk.gov.register.protocols");
    }

    @Override
    public void run(RegisterConfiguration configuration, Environment environment) throws Exception {
        DBIFactory dbiFactory = new DBIFactory();

        JerseyEnvironment jersey = environment.jersey();
        DropwizardResourceConfig resourceConfig = jersey.getResourceConfig();
        Client client = new JerseyClientBuilder(environment).using(configuration.getJerseyClientConfiguration()).build("http-client");

        Optional<String> registersYamlFileUrl = Optional.ofNullable(System.getProperty("registersYaml"));
        Optional<String> fieldsYamlFileUrl = Optional.ofNullable(System.getProperty("fieldsYaml"));

        ConfigManager configManager = new ConfigManager(configuration, registersYamlFileUrl, fieldsYamlFileUrl);
        configManager.refreshConfig();

        RegistersConfiguration registersConfiguration = configManager.createRegistersConfiguration();
        FieldsConfiguration mintFieldsConfiguration = configManager.createFieldsConfiguration();

        AllTheRegisters allTheRegisters = configuration.getAllTheRegisters().build(dbiFactory, registersConfiguration, mintFieldsConfiguration, environment);
        allTheRegisters.stream().forEach(register -> register.getFlyway().migrate());

        jersey.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(mintFieldsConfiguration).to(FieldsConfiguration.class);
                bind(registersConfiguration).to(RegistersConfiguration.class);
                bindFactory(RegisterDataFactory.class).to(RegisterData.class);
                bindFactory(RegisterFieldsConfigurationFactory.class).to(RegisterFieldsConfiguration.class);
                bind(allTheRegisters);
                bindFactory(RegisterContextProvider.class).to(RegisterContext.class);
                bindAsContract(RegisterFieldsConfiguration.class);
                bind(new PublicBodiesConfiguration(Optional.ofNullable(System.getProperty("publicBodiesYaml")))).to(PublicBodiesConfiguration.class);

                bind(CanonicalJsonMapper.class).to(CanonicalJsonMapper.class);
                bind(CanonicalJsonValidator.class).to(CanonicalJsonValidator.class);
                bind(ObjectReconstructor.class).to(ObjectReconstructor.class);
                bind(RegisterSerialisationFormatService.class).to(RegisterSerialisationFormatService.class);

                bind(RequestContext.class).to(RequestContext.class).to(SchemeContext.class);
                bind(ViewFactory.class).to(ViewFactory.class).in(Singleton.class);
                bind(ItemConverter.class).to(ItemConverter.class).in(Singleton.class);
                bind(GovukOrganisationClient.class).to(GovukOrganisationClient.class).in(Singleton.class);

                bindFactory(Factories.PostgresRegisterFactory.class).to(Register.class).to(RegisterReadOnly.class);
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


