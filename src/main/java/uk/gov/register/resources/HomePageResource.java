package uk.gov.register.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.views.View;
import org.glassfish.hk2.api.IterableProvider;
import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.indexer.function.IndexFunction;
import uk.gov.register.service.IndexService;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
@Timed
public class HomePageResource {
    private final RegisterReadOnly register;
    private final ViewFactory viewFactory;
    private final RegisterTrackingConfiguration config;
    private IndexService indexService;

    @Inject
    private IterableProvider<IndexFunction> indexFunctions;

    @Inject
    public HomePageResource(RegisterReadOnly register, ViewFactory viewFactory, RegisterTrackingConfiguration config) {
        this.register = register;
        this.viewFactory = viewFactory;
        this.config = config;
//        this.indexService = indexService;
    }

    @GET
    @Produces({ExtraMediaType.TEXT_HTML})
    @Timed
    public View home() {
//        ServiceLocator serviceLocator = ServiceLocatorUtilities.createAndPopulateServiceLocator();
//        IndexService indexService = serviceLocator.getService(IndexService.class);
        int size = indexFunctions.getSize();
        indexService.test();

        return viewFactory.homePageView(
                register.getTotalRecords(),
                register.getTotalEntries(),
                register.getLastUpdatedTime()
        );
    }

    @GET
    @Path("/robots.txt")
    @Produces(MediaType.TEXT_PLAIN)
    @Timed
    public String robots() {
        return "User-agent: *\n" +
                "Disallow: /\n";
    }

    @GET
    @Path("/analytics-code.js")
    @Produces(ExtraMediaType.APPLICATION_JAVASCRIPT)
    @Timed
    public String analyticsTrackingId() {
        return config.getRegisterTrackingId().map(
                trackingId -> "var gaTrackingId = \"" + trackingId + "\";\n"
        ).orElse("");
    }
}
