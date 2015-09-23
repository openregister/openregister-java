package uk.gov.indexer;

import org.skife.jdbi.v2.DBI;
import uk.gov.indexer.dao.DBQueryDAO;
import uk.gov.indexer.dao.DestinationDBQueryDAO;
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
    private final static List<DBQueryDAO> databaseObjectRegistry = new ArrayList<>();

    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
        Configuration configuration = new Configuration(args);
        Set<String> registers = configuration.getRegisters();

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(registers.size());
        addShutdownHook(executorService);

        for (String register : registers) {
            try {
                DestinationDBQueryDAO destinationQueryDAO = createDestinationDAO(register, configuration);
                SourceDBQueryDAO sourceQueryDAO = createSourceDAO(register, configuration);

                executorService.scheduleAtFixedRate(new IndexerTask(register, sourceQueryDAO, destinationQueryDAO), 0, 10, TimeUnit.SECONDS);
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

    private static DestinationDBQueryDAO createDestinationDAO(String register, Configuration configuration) {
        DBI dbi = new DBI(configuration.getProperty(register + ".destination.postgres.db.connectionString"));
        DestinationDBQueryDAO destinationDBQueryDAO = dbi.onDemand(DestinationDBQueryDAO.class);
        databaseObjectRegistry.add(destinationDBQueryDAO);
        return destinationDBQueryDAO;
    }


    private static void addShutdownHook(final ScheduledExecutorService executorService) {
        runtime.addShutdownHook(new Thread() {
            @Override
            public void run() {
                executorService.shutdown();
                databaseObjectRegistry.forEach(DBQueryDAO::close);
                ConsoleLogger.log("Shutdown completed...");
            }
        });
    }
}
