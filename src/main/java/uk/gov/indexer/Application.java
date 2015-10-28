package uk.gov.indexer;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

public class Application {
    public static void main(String[] args) throws InterruptedException {
        Configuration configuration = new Configuration(args);
        Set<String> registers = configuration.getRegisters();

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool((int) registers.stream().filter(r -> configuration.cloudSearchEndPoint(r).isPresent()).count() + registers.size());

        Stream<Indexer> indexerStream = registers.stream().map(r -> new Indexer(configuration, r));

        addShutdownHook(executorService, indexerStream);

        indexerStream.parallel().forEach(indexer -> indexer.start(executorService));

        Thread.currentThread().join();
    }

    private static void addShutdownHook(final ScheduledExecutorService executorService, Stream<Indexer> indexerStream) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                executorService.shutdown();
                indexerStream.forEach(Indexer::stop);
                ConsoleLogger.log("Shutdown completed...");
            }
        });
    }
}
