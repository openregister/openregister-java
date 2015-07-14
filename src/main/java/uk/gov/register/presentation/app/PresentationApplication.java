package uk.gov.register.presentation.app;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.dropwizard.views.mustache.MustacheViewRenderer;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.server.ServerProperties;
import org.skife.jdbi.v2.DBI;
import uk.gov.register.presentation.config.PresentationConfiguration;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.dao.RecentEntryIndexUpdateDAO;
import uk.gov.register.presentation.resource.*;

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
        bootstrap.addBundle(new ViewBundle<>(ImmutableList.of(new MustacheViewRenderer())));
        bootstrap.addBundle(new AssetsBundle("/assets"));
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

        JerseyEnvironment jersey = environment.jersey();
        jersey.register(new DataResource(queryDAO));
        jersey.register(new HomePageResource());
        jersey.register(new SearchResource(queryDAO));

        MutableServletContextHandler applicationContext = environment.getApplicationContext();
        applicationContext.addFilter(RepresentationFilter.class, "/*", EnumSet.allOf(DispatcherType.class));

        setCorsPreflight(applicationContext);
    }

    private void setCorsPreflight(MutableServletContextHandler applicationContext) {
        FilterHolder filterHolder = applicationContext
                .addFilter(CrossOriginFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin");
        filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "OPTIONS,GET,PUT,POST,DELETE,HEAD");
    }
}
