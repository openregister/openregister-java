package uk.gov.indexer;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import uk.gov.indexer.dao.DBConnectionDAO;
import uk.gov.indexer.dao.DestinationDBUpdateDAO;
import uk.gov.indexer.dao.IndexedEntriesUpdateDAO;
import uk.gov.indexer.dao.SourceDBQueryDAO;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Application {
    private final static Runtime runtime = Runtime.getRuntime();
    private final static List<DBConnectionDAO> databaseObjectRegistry = new ArrayList<>();

    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
        Configuration configuration = new Configuration(args);
        Set<String> registers = configuration.getRegisters();

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool((int) registers.stream().filter(r -> configuration.cloudSearchEndPoint(r).isPresent()).count() + registers.size());

        addShutdownHook(executorService);

        for (String register : registers) {
            try {
                ConsoleLogger.log("setting up register " + register);
                DBI dbi = new DBI(configuration.getProperty(register + ".destination.postgres.db.connectionString"));

                DestinationDBUpdateDAO destinationDBUpdateDAO = createDestinationDBUpdateDAO(dbi);

                executorService.scheduleAtFixedRate(
                        new IndexerTask(register, createSourceDAO(register, configuration), destinationDBUpdateDAO),
                        0,
                        10,
                        TimeUnit.SECONDS
                );

                configuration.cloudSearchEndPoint(register).ifPresent(
                        endPoint -> executorService.scheduleAtFixedRate(
                                new CloudSearchDataUploadTask(register, endPoint, configuration.cloudSearchWaterMarkEndPoint(register).get(), dbi.onDemand(IndexedEntriesUpdateDAO.class)),
                                0,
                                5,
                                TimeUnit.MINUTES
                        )
                );


            } catch (Throwable e) {
                e.printStackTrace();
                ConsoleLogger.log("Error occurred while setting indexer for register: " + register + ". Error is -> " + e.getMessage());
            }
        }

        Thread.currentThread().join();
    }

    private static SourceDBQueryDAO createSourceDAO(String register, Configuration configuration) {
        DBI dbi = new DBI(configuration.getProperty(register + ".source.postgres.db.connectionString"));
        SourceDBQueryDAO sourceDBQueryDAO = dbi.onDemand(SourceDBQueryDAO.class);
        databaseObjectRegistry.add(sourceDBQueryDAO);
        return sourceDBQueryDAO;
    }

    private static DestinationDBUpdateDAO createDestinationDBUpdateDAO(DBI dbi) {
        Handle handle = dbi.open();
        DestinationDBUpdateDAO destinationDBUpdateDAO = handle.attach(DestinationDBUpdateDAO.class);
        databaseObjectRegistry.add(destinationDBUpdateDAO);
        return destinationDBUpdateDAO;
    }


    private static void addShutdownHook(final ScheduledExecutorService executorService) {
        runtime.addShutdownHook(new Thread() {
            @Override
            public void run() {
                executorService.shutdown();
                databaseObjectRegistry.forEach(DBConnectionDAO::close);
                ConsoleLogger.log("Shutdown completed...");
            }
        });
    }
}
