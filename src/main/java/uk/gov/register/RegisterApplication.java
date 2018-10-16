package uk.gov.register;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.configuration.UrlConfigurationSourceProvider;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.CommonProperties;
import uk.gov.ida.dropwizard.logstash.LogstashBundle;
import uk.gov.organisation.client.GovukOrganisationClient;
import uk.gov.register.auth.BasicAuthFilter;
import uk.gov.register.auth.RegisterAuthDynamicFeature;
import uk.gov.register.configuration.*;
import uk.gov.register.core.*;
import uk.gov.register.db.Factories;
import uk.gov.register.filters.CorsBundle;
import uk.gov.register.filters.HttpToHttpsRedirectFilter;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.resources.SchemeContext;
import uk.gov.register.serialization.RSFCreator;
import uk.gov.register.serialization.RSFExecutor;
import uk.gov.register.serialization.handlers.AddItemCommandHandler;
import uk.gov.register.serialization.handlers.AppendEntryCommandHandler;
import uk.gov.register.serialization.handlers.AssertRootHashCommandHandler;
import uk.gov.register.serialization.mappers.EntryToCommandMapper;
import uk.gov.register.serialization.mappers.ItemToCommandMapper;
import uk.gov.register.serialization.mappers.RootHashCommandMapper;
import uk.gov.register.service.EnvironmentValidator;
import uk.gov.register.service.BlobConverter;
import uk.gov.register.service.RegisterSerialisationFormatService;
import uk.gov.register.thymeleaf.ThymeleafViewRenderer;
import uk.gov.register.util.CanonicalJsonMapper;
import uk.gov.register.util.CanonicalJsonValidator;
import uk.gov.register.util.ObjectReconstructor;
import uk.gov.register.views.ViewFactory;

import javax.inject.Singleton;
import javax.servlet.DispatcherType;
import javax.ws.rs.client.Client;
import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RegisterApplication extends Application<RegisterConfiguration> {
    public static void main(String[] args) {
        try {
            new RegisterApplication().run(args);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    private static boolean isRunningOnCloudFoundry() {
        return System.getenv().containsKey("CF_INSTANCE_GUID");
    }

    @Override
    public String getName() {
        return "openregister";
    }

    @Override
    public void initialize(Bootstrap<RegisterConfiguration> bootstrap) {
        bootstrap.addBundle(new ViewBundle<>(ImmutableList.of(new ThymeleafViewRenderer("HTML5", "/templates/", ".html", false))));

        if (isRunningOnCloudFoundry()) {
            bootstrap.setConfigurationSourceProvider(new UrlConfigurationSourceProvider());
        }

        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                ));
        bootstrap.addBundle(new AssetsBundle("/assets"));
        bootstrap.addBundle(new CorsBundle());
        bootstrap.addBundle(new LogstashBundle());

        System.setProperty("java.protocol.handler.pkgs", "uk.gov.register.protocols");
    }

    @Override
    public void run(RegisterConfiguration configuration, Environment environment) throws Exception {
        JerseyEnvironment jersey = environment.jersey();
        jersey.property(CommonProperties.OUTBOUND_CONTENT_LENGTH_BUFFER, 0);

        DropwizardResourceConfig resourceConfig = jersey.getResourceConfig();
        Client client = new JerseyClientBuilder(environment).using(configuration.getJerseyClientConfiguration()).build("http-client");

        ConfigManager configManager = new ConfigManager(configuration);
        configManager.refreshConfig();

        EnvironmentValidator environmentValidator = new EnvironmentValidator(configManager);

        DBIFactory dbiFactory = new DBIFactory();
        DatabaseManager databaseManager = new DatabaseManager(configuration, environment, dbiFactory, isRunningOnCloudFoundry());

        AllTheRegisters allTheRegisters = configuration.getAllTheRegisters().build(configManager, databaseManager, environmentValidator, configuration);
        allTheRegisters.stream().forEach(registerContext -> {
            registerContext.migrate();
            registerContext.validate();
            
            CompletableFuture.runAsync(() -> registerContext.buildOnDemandRegister().getRegisterProof());
        });

        RSFExecutor rsfExecutor = new RSFExecutor();
        rsfExecutor.register(new AddItemCommandHandler());
        rsfExecutor.register(new AppendEntryCommandHandler());
        rsfExecutor.register(new AssertRootHashCommandHandler());

        RSFCreator rsfCreator = new RSFCreator();
        rsfCreator.register(new ItemToCommandMapper());
        rsfCreator.register(new EntryToCommandMapper());
        rsfCreator.register(new RootHashCommandMapper());

        environment.servlets()
                .addFilter("HttpToHttpsRedirectFilter", new HttpToHttpsRedirectFilter())
                .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");

        jersey.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(environment.healthChecks()).to(HealthCheckRegistry.class);
                bind(allTheRegisters);
                bindFactory(Factories.RegisterContextProvider.class).to(RegisterContext.class)
                        .to(DeleteRegisterDataConfiguration.class).to(ResourceConfiguration.class)
                        .to(HomepageContentConfiguration.class).to(IndexConfiguration.class);
                bindAsContract(RegisterFieldsConfiguration.class);

                bind(configManager).to(ConfigManager.class);
                bind(environmentValidator).to(EnvironmentValidator.class);
                bind(new PublicBodiesConfiguration(Optional.ofNullable(System.getProperty("publicBodiesYaml")))).to(PublicBodiesConfiguration.class);

                bind(CanonicalJsonMapper.class).to(CanonicalJsonMapper.class);
                bind(CanonicalJsonValidator.class).to(CanonicalJsonValidator.class);
                bind(ObjectReconstructor.class).to(ObjectReconstructor.class);
                bind(rsfExecutor).to(RSFExecutor.class);
                bind(rsfCreator).to(RSFCreator.class);
                bind(RegisterSerialisationFormatService.class).to(RegisterSerialisationFormatService.class);

                bind(RequestContext.class).to(RequestContext.class).to(SchemeContext.class);
                bindFactory(Factories.RegisterIdProvider.class).to(RegisterId.class);
                bind(ViewFactory.class).to(ViewFactory.class).in(Singleton.class);
                bind(BlobConverter.class).to(BlobConverter.class).in(Singleton.class);
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
        jersey.register(new RegisterAuthDynamicFeature(BasicAuthFilter.class));

        environment.getApplicationContext().setErrorHandler(new AssetsBundleCustomErrorHandler(environment));
    }
}


