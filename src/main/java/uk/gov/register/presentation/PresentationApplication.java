package uk.gov.register.presentation;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.Application;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.server.ServerProperties;
import org.skife.jdbi.v2.DBI;

import javax.servlet.DispatcherType;
import javax.ws.rs.core.MediaType;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PresentationApplication extends Application<PresentationConfiguration> {

    public static void main(String[] args) throws Exception {
        new PresentationApplication().run(args);
    }

    @Override
    public String getName() {
        return "presentation";
    }

    @Override
    public void initialize(Bootstrap<PresentationConfiguration> bootstrap) {
    }

    @Override
    public void run(PresentationConfiguration configuration, Environment environment) throws Exception {
        DBIFactory dbiFactory = new DBIFactory();
        DBI jdbi = dbiFactory.build(environment, configuration.getDatabase(), "postgres");
        RecentEntryIndexUpdateDAO updateDAO = jdbi.onDemand(RecentEntryIndexUpdateDAO.class);
        RecentEntryIndexQueryDAO queryDAO = jdbi.onDemand(RecentEntryIndexQueryDAO.class);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new ConsumerRunnable(configuration, updateDAO));

        DropwizardResourceConfig resourceConfig = environment.jersey().getResourceConfig();
        resourceConfig.property(ServerProperties.MEDIA_TYPE_MAPPINGS, ImmutableMap.of(
                "json", MediaType.APPLICATION_JSON_TYPE,
                "xml", MediaType.APPLICATION_XML_TYPE));

        environment.jersey().register(new PresentationResource(queryDAO));
        setCorsPreflight(environment);
    }

    private void setCorsPreflight(Environment environment) {
        FilterHolder filterHolder = environment.getApplicationContext()
                .addFilter(CrossOriginFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin");
        filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "OPTIONS,GET,PUT,POST,DELETE,HEAD");
    }
}
