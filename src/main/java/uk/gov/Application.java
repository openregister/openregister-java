package uk.gov;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

public class Application {

    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
        Properties  properties = new Properties();
        properties.load(Application.class.getResourceAsStream("/application.properties"));

        SourcePostgresDB sourceDB = new SourcePostgresDB(properties.getProperty("source.postgres.db.connectionString"));
        DestinationPostgresDB destinationDB = new DestinationPostgresDB(properties.getProperty("destination.postgres.db.connectionString"));

        Indexer indexer = new Indexer(sourceDB, destinationDB);

        //noinspection InfiniteLoopStatement
        while (true) {
            ConsoleLogger.log("Starting index update...");
            indexer.update();
            ConsoleLogger.log("Index update completed.");
            ConsoleLogger.log("Waiting 10 seconds......");
            Thread.sleep(10000);
        }
    }
}
