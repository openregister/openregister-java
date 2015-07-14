package uk.gov.register.presentation.app;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.dropwizard.views.mustache.MustacheViewRenderer;
import org.apache.commons.dbcp2.BasicDataSource;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.server.ServerProperties;
import org.skife.jdbi.v2.DBI;
import uk.gov.register.presentation.config.PresentationConfiguration;
import uk.gov.register.presentation.dao.DB;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.dao.RecentEntryIndexUpdateDAO;
import uk.gov.register.presentation.resource.HomePageResource;
import uk.gov.register.presentation.resource.LatestFeedResource;
import uk.gov.register.presentation.resource.SearchResource;

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

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(configuration.getDatabase().getDriverClass());
        dataSource.setUrl(configuration.getDatabase().getUrl());

        RecentEntryIndexQueryDAO queryDAO = new RecentEntryIndexQueryDAO(new DB(dataSource));

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new ConsumerRunnable(configuration, updateDAO));

        DropwizardResourceConfig resourceConfig = environment.jersey().getResourceConfig();
        resourceConfig.property(ServerProperties.MEDIA_TYPE_MAPPINGS, ImmutableMap.of(
                "json", MediaType.APPLICATION_JSON_TYPE,
                "xml", MediaType.APPLICATION_XML_TYPE));

        JerseyEnvironment jersey = environment.jersey();
        jersey.register(new LatestFeedResource(queryDAO));
        jersey.register(new HomePageResource());
        jersey.register(new SearchResource(queryDAO));
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
