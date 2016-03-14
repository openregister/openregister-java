package uk.gov.register.presentation.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.java8.Java8Bundle;
import io.dropwizard.java8.jdbi.DBIFactory;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ServerProperties;
import org.skife.jdbi.v2.DBI;
import uk.gov.organisation.client.GovukOrganisationClient;
import uk.gov.register.presentation.ContentSecurityPolicyFilter;
import uk.gov.register.presentation.ContentTypeOptionsFilter;
import uk.gov.register.presentation.EntryConverter;
import uk.gov.register.presentation.XssProtectionFilter;
import uk.gov.register.presentation.config.FieldsConfiguration;
import uk.gov.register.presentation.config.PresentationConfiguration;
import uk.gov.register.presentation.config.PublicBodiesConfiguration;
import uk.gov.register.presentation.config.RegistersConfiguration;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.representations.*;
import uk.gov.register.presentation.resource.*;
import uk.gov.register.presentation.view.ViewFactory;
import uk.gov.register.thymeleaf.ThymeleafViewRenderer;

import javax.inject.Singleton;
import javax.servlet.DispatcherType;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import java.util.EnumSet;
import java.util.Optional;

public class PresentationApplication extends Application<PresentationConfiguration> {

    public static void main(String[] args) throws Exception {
        new PresentationApplication().run(args);
    }

    @Override
    public String getName() {
        return "presentation";
    }

    public static ObjectMapper customObjectMapper() {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        objectMapper.registerModules(new Jdk8Module(), new JavaTimeModule());
        return objectMapper;
    }

    @Override
    public void initialize(Bootstrap<PresentationConfiguration> bootstrap) {
        bootstrap.addBundle(new ViewBundle<>(ImmutableList.of(new ThymeleafViewRenderer("HTML5", "/templates/", ".html", false))));
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                ));
        bootstrap.addBundle(new AssetsBundle("/assets"));
        bootstrap.addBundle(new Java8Bundle());
        bootstrap.setObjectMapper(customObjectMapper());
    }

    @Override
    public void run(PresentationConfiguration configuration, Environment environment) throws Exception {
        DBIFactory dbiFactory = new DBIFactory();
        DBI jdbi = dbiFactory.build(environment, configuration.getDatabase(), "postgres");
        RecentEntryIndexQueryDAO queryDAO = jdbi.onDemand(RecentEntryIndexQueryDAO.class);

        JerseyEnvironment jerseyEnvironment = environment.jersey();
        DropwizardResourceConfig resourceConfig = jerseyEnvironment.getResourceConfig();

        ImmutableMap<String, MediaType> representations = ImmutableMap.of(
                "csv", ExtraMediaType.TEXT_CSV_TYPE,
                "tsv", ExtraMediaType.TEXT_TSV_TYPE,
                "ttl", ExtraMediaType.TEXT_TTL_TYPE,
                "json", MediaType.APPLICATION_JSON_TYPE,
                "yaml", ExtraMediaType.TEXT_YAML_TYPE
        );
        resourceConfig.property(ServerProperties.MEDIA_TYPE_MAPPINGS, representations);

        Client client = new JerseyClientBuilder(environment).using(configuration.getJerseyClientConfiguration())
                .build("http-client");

        jerseyEnvironment.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(queryDAO).to(RecentEntryIndexQueryDAO.class);

                bind(new FieldsConfiguration(Optional.ofNullable(System.getProperty("fieldsYaml")))).to(FieldsConfiguration.class);
                bind(new RegistersConfiguration(Optional.ofNullable(System.getProperty("registersYaml")))).to(RegistersConfiguration.class);
                bind(new PublicBodiesConfiguration(Optional.ofNullable(System.getProperty("publicBodiesYaml")))).to(PublicBodiesConfiguration.class);

                bind(RequestContext.class).to(RequestContext.class);
                bind(ViewFactory.class).to(ViewFactory.class).in(Singleton.class);
                bind(EntryConverter.class).to(EntryConverter.class).in(Singleton.class);
                bind(GovukOrganisationClient.class).to(GovukOrganisationClient.class).in(Singleton.class);
                bind(configuration);
                bind(client).to(Client.class);
            }
        });


        resourceConfig.packages("uk.gov.register.presentation.resource");

        jerseyEnvironment.register(CsvWriter.class);
        jerseyEnvironment.register(TsvWriter.class);
        jerseyEnvironment.register(TurtleWriter.class);
        jerseyEnvironment.register(YamlWriter.class);

        jerseyEnvironment.register(ClientErrorExceptionMapper.class);
        jerseyEnvironment.register(ThrowableExceptionMapper.class);

        jerseyEnvironment.register(CacheNoTransformFilterFactory.class);

        MutableServletContextHandler applicationContext = environment.getApplicationContext();

        setCorsPreflight(applicationContext);
        jerseyEnvironment.register(ContentSecurityPolicyFilter.class);
        jerseyEnvironment.register(ContentTypeOptionsFilter.class);
        jerseyEnvironment.register(XssProtectionFilter.class);
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
