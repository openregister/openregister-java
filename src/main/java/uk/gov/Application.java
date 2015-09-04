package uk.gov;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Application {
    private final static Runtime runtime = Runtime.getRuntime();
    private final static List<PostgresDB> databaseObjectRegistry = new ArrayList<>();

    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
        Configuration configuration = new Configuration(args);
        Set<String> registers = configuration.getRegisters();

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(registers.size());
        addShutdownHook(executorService);

        for (String register : registers) {
            SourcePostgresDB sourceDB = createSourceDBObject(register, configuration);
            DestinationPostgresDB destinationDB = createDestinationDBObject(register, configuration);
            executorService.scheduleAtFixedRate(new IndexerTask(register, sourceDB, destinationDB), 0, 10, TimeUnit.SECONDS);
        }

        Thread.currentThread().join();
    }


    private static void addShutdownHook(final ScheduledExecutorService executorService) {
        runtime.addShutdownHook(new Thread() {
            @Override
            public void run() {
                executorService.shutdown();
                databaseObjectRegistry.forEach(PostgresDB::closeConnection);
                ConsoleLogger.log("Shutdown completed...");
            }
        });
    }

    private static DestinationPostgresDB createDestinationDBObject(String register, Configuration configuration) throws SQLException {
        DestinationPostgresDB destinationDB = new DestinationPostgresDB(configuration.getProperty(register + ".destination.postgres.db.connectionString"));
        databaseObjectRegistry.add(destinationDB);
        return destinationDB;
    }

    private static SourcePostgresDB createSourceDBObject(String register, Configuration configuration) throws SQLException {
        SourcePostgresDB sourceDB = new SourcePostgresDB(configuration.getProperty(register + ".source.postgres.db.connectionString"));
        databaseObjectRegistry.add(sourceDB);
        return sourceDB;
    }
}
