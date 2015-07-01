package uk.gov.register.presentation;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.Application;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.server.ServerProperties;

import javax.ws.rs.core.MediaType;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

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
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        AtomicReference<byte[]> currentLatest = new AtomicReference<>();
        executorService.execute(new ConsumerRunnable(currentLatest, configuration));

        DropwizardResourceConfig resourceConfig = environment.jersey().getResourceConfig();
        resourceConfig.property(ServerProperties.MEDIA_TYPE_MAPPINGS, ImmutableMap.of(
                "json", MediaType.APPLICATION_JSON_TYPE,
                "xml", MediaType.APPLICATION_XML_TYPE));

        environment.jersey().register(new PresentationResource(currentLatest));
    }
}
