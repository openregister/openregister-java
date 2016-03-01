package uk.gov.indexer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.indexer.monitoring.CloudwatchProcessHeartbeat;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws InterruptedException {
        Configuration configuration = new Configuration(args);
        Set<String> registers = configuration.getRegisters();

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool((int) registers.stream().filter(r -> configuration.cloudSearchEndPoint(r).isPresent() || configuration.elasticSerachEndPoint(r).isPresent()).count() + registers.size());

        ScheduledExecutorService cloudwatchExecutorService = Executors.newScheduledThreadPool(1);

        List<Indexer> indexers = registers.stream().map(r -> new Indexer(configuration, r)).collect(Collectors.toList());

        addShutdownHook(executorService, cloudwatchExecutorService, indexers);

        indexers.stream().parallel().forEach(indexer -> indexer.start(executorService));

        configuration.cloudwatchEnvironmentName().ifPresent(e ->
                        cloudwatchExecutorService.scheduleAtFixedRate(
                                new CloudwatchProcessHeartbeat(e),
                                0,
                                10,
                                TimeUnit.SECONDS
                        )
        );

        Thread.currentThread().join();
    }

    private static void addShutdownHook(final ScheduledExecutorService executorService, ScheduledExecutorService cloudwatchExecutorService, List<Indexer> indexers) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                cloudwatchExecutorService.shutdown();

                executorService.shutdown();

                indexers.forEach(Indexer::stop);

                LOGGER.info("Shutdown completed...");
            }
        });
    }
}
