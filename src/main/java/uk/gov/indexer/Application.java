package uk.gov.indexer;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public class Application {
    public static void main(String[] args) throws InterruptedException {
        Configuration configuration = new Configuration(args);
        Set<String> registers = configuration.getRegisters();

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool((int) registers.stream().filter(r -> configuration.cloudSearchEndPoint(r).isPresent() || configuration.elasticSerachEndPoint(r).isPresent()).count() + registers.size());

        List<Indexer> indexers = registers.stream().map(r -> new Indexer(configuration, r)).collect(Collectors.toList());

        addShutdownHook(executorService, indexers);

        indexers.stream().parallel().forEach(indexer -> indexer.start(executorService));

        Thread.currentThread().join();
    }

    private static void addShutdownHook(final ScheduledExecutorService executorService, List<Indexer> indexers) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                executorService.shutdown();
                indexers.forEach(Indexer::stop);
                ConsoleLogger.log("Shutdown completed...");
            }
        });
    }
}
